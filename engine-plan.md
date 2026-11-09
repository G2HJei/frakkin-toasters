# Engine plan

Implements roadmap step 4: the game engine that drives events and turns. The journal moves out of `Game` and into the
engine. The turn state stays in `Game`, grouped in a `Turn`.

Every phase ends with the tests green. The plan is done when a seeded game can be played through `submit` and `choose`
alone, from setup to either ending, and `restore` gives the same game at every pause.

## Target design

```text
                             frontend / tests
                                    |
         submit(player, event)      |      status(player)  Waiting | Finished
         choose(player, option)     |      view()          GameView
                                    v
+-----------------------------------+--------------------------------------+
|                                GameEngine                                |
|                                                                          |
|  +----------------------------+  accepted  +-------------------------+   |
|  | Input check                +----------->|        Run loop         |   |
|  | player, type, constraints  |            |                         |   |
|  +----------------------------+            | 1 take next event       |   |
|                                            | 2 validate              |   |
|  +----------------------------+    next    | 3 open scope            |   |
|  | Agenda                     +----------->| 4 process               |   |
|  | stack of pending followups |<-----------+ 5 win checks            |   |
|  +----------------------------+  followup  | 6 close scope           |   |
|                                            | 7 push followup         |   |
|  +----------------------------+   create   |                         |   |
|  | Processor registry         |<-----------+ stops at a decision     |   |
|  +----------------------------+            | or when the game ends   |   |
|                                            +---+-----------------+---+   |
|  +----------------------------+  scopes,       |                 |       |
|  | Journal                    |  followups,    |                 |       |
|  | scopes, facts, followups,  |  choices       |                 |       |
|  | choices                    |<---------------+                 |       |
|  +-------------^--------------+                                  |       |
|                | append                                          |       |
|  +-------------+--------------+                                  |       |
|  | FactRecorder               |                                  |       |
|  | apply, then append         |                                  |       |
|  +----+-----------------^-----+                                  |       |
|       |                 |                                        |       |
+-------+-----------------+----------------------------------------+-------+
        |                 |                                        |
        | apply           | record(fact)                           | event
        |                 |                                        v
        |    +------------+-------------+  calls   +---------------+-------+
        |    | GameOps                  |<---------+ XxxEventProcessor     |
        |    | PlayerOps, ShipOps, ...  |          | prepare(): view only  |
        |    +--------------------------+          | run(ops): view + ops  |
        v                                          +---------------+-------+
+-------+----------------------------------+                       |
| Game                                     |  reads                |
| boards, decks, players, ships, titles    |<----------------------+
|                                          |  GameView
| +--------------------------------------+ |
| | Turn                                 | |
| | number, step, current, acting player | |
| +--------------------------------------+ |
+------------------------------------------+
```

The engine has two public moves. `submit` answers a pending decision with a concrete event, and `choose` picks an option
of a pending `OneOf`. Both run the game forward until the next point where a player has to act, so after every call the
engine is either `Waiting(PendingInput)` or `Finished(winner)`. There is no "running" state to observe. `status` takes
the viewing player, because the input of an interrupt window is secret (decision 8).

`GameEngine.start(playerCount, randomness)` begins a game with `StartGameEvent` on the agenda, and
`GameEngine.restore(journal)` rebuilds one. `PendingInput` isn't stored. It is read from the top of the agenda, so
restoring the agenda restores it too.

### Turn cycle

The turn sequence is ordinary events on the agenda (rule 8.1). The engine knows nothing about steps.

```text
StartGameEvent --> setup followups
                         |
                         v
                  StartTurnEvent <--------------------------+
                         |                                  |
                         v                                  |
                   Receive Skills                           |
                         |                                  |
                         v                                  |
                      Movement                              |
                         |                                  |
                         v                                  |
                       Action                               |
                         |                                  |
                         v                                  |
                       Crisis                               |
                         |                                  |
                         v                                  |
              Activate Cylon Ships (icon)                   |
                         |                                  |
                         v                                  |
                Prepare for Jump (icon)                     |
                         |                                  |
                         v                                  |
                      Discard                               |
                         |                                  |
                         v                                  |
                    EndTurnEvent ------ next player --------+
                         |
                         | a resource is at 0
                         v
                     Cylons win
```

Each step's processor decides whether it applies. The other endings, the immediate Cylon wins and the Kobol end game,
leave the cycle from wherever they happen.

### Why a stack

Followups have to run depth first. If `all(A, B)` is queued and `A` returns `C`, then `C` runs before `B`. The skill
check depends on this: everything a result queues has to finish before `EndSkillCheckEvent` clears the check. A FIFO
queue would run `B` first, so the agenda is a stack of `(followup, parentScope)` frames, and `AllOf` pushes its children
in reverse order.

| Top of the agenda             | What the run loop does             |
|-------------------------------|------------------------------------|
| `None`                        | Pops it                            |
| `AllOf`                       | Pops it and pushes the children    |
| `Single(event)`               | Pops it and executes the event     |
| `Single(PlayerDecisionEvent)` | Stops: `Waiting(AwaitingDecision)` |
| `OneOf`                       | Stops: `Waiting(AwaitingChoice)`   |

## Decisions

1. **Turn state stays in `Game`.** `Game` holds a `Turn` with the turn number, step, current player and acting player,
   read through a new `TurnView`. Rules read this state and replay has to restore it, so it sits with the rest of the
   state and `FactApplier` keeps a single target. The engine only advances it, by running the turn events.
2. **The journal moves to the engine.** `journal/` becomes `engine/journal/`, and the engine owns the instance. Ops get
   a `FactRecorder`, which replaces `JournalWriter` and the apply-then-append logic in `AbstractOps.record`.
3. **The engine holds no game rules.** Steps, skips and exceptions are decisions, so they belong in processors, as
   `CLAUDE.md` already says. The turn events live in `event/turn/` and `WinChecks` in `event/endgame/`, and the engine
   only runs them.
4. **One pending input at a time.** Every "each player" sequence in the code is already sequential, and interrupt
   windows ask one player at a time too, so nothing needs parallel decisions.
5. **The journal rebuilds everything.** Facts rebuild `Game`, including its `Turn`, and scope entries rebuild the
   agenda. This closes the gap `journal-plan.md` left open for unresolved followups.
6. **`submit` and `choose` are all-or-nothing.** Validation runs before the scope opens, so a plain rejection touches
   nothing. If anything throws after that, the engine rebuilds the game from the journal as it stood before the call,
   and the same input stays pending. Facts apply as they are recorded, so there is no other way to undo a half-finished
   chain of events.
7. **The `OneOf` chooser is derived, not stored.** It is the player of the first `PlayerEvent` found in the options,
   otherwise the current player. This is sound because `PlayerEvent.playerNumber()` is always the acting player: events
   that name a target, such as `SendToBrigEvent`, are plain `Event`s. It covers every `one(...)` in the code today. The
   engine throws `FrakCallTheAdmiralException` if the options name different players. `Followup` and its tests don't
   change.
8. **Interrupt windows ask in secret.** Only the players holding a card for the window are asked. Everyone else sees a
   plain "waiting", not who it is waiting for. Asking everyone before and after every die roll would make a Cylon attack
   unplayable.

## Phase 1 - Detach the journal from `Game`

A pure refactor, with no behavior change.

- Remove `Game.journal`. Only `AbstractOps` reads it in main, and 6 test classes read it (28 lines).
- Move `journal/` to `engine/journal/`, including `fact/`.
- Add `FactRecorder(game, journal)`, which applies a fact and then appends it. Delete `JournalWriter`.
- Pass it through the ops: `new GameOps(game, recorder)`, and the same for each subclass constructor.
- Change `EventProcessor.execute` to take the ops instead of building them, so it no longer needs a concrete `Game`.
- `TestHarness` owns `journal` and `recorder`. Tests change from `game.journal()` to `journal`.
- `ArchitectureTest`: add `..engine..` to `stateDoesNotDependOnOpsEventsOrTheEngine`. The rule's name already promises
  it, but today `Game` reaches `event` through `Journal`. Add a rule that `..event..` doesn't depend on `..engine..`, so
  processors never see the journal. Nothing in `event/` imports it today.

## Phase 2 - Group turn state into `Turn`

A small refactor, with no behavior change. It follows the pattern of `boards()`.

- Move `Turn` from the root package to `state/`. It holds `number`, `step`, `currentPlayerNumber` and
  `actingPlayerNumber`, and `number` counts player turns, not rounds. Its setters are `@Mutates`.
- Add `state/view/TurnView`. `Game` gets a `Turn` field, and `GameView.turn()` returns the `TurnView` instead of an
  `int`. This resolves the `todo add turns` in `Game`.
- Reads change to `game.turn().step()`, `game.turn().number()` and so on: about 20 lines in main and 37 in tests.
  `GameView` keeps `currentPlayer()` and `actingPlayer()`, because `Turn` can't look up a player.
- `FactApplier` writes to `game.turn()`. The facts stay in `GameFact` and the methods stay in `GameOps`, so
  `FactApplier.apply`, `GameReplayer` and `EventProcessor` keep their signatures.
- `TestHarness` copies `template.turn()`, and `GameSnapshot` reads through `game.turn()`.

## Phase 3 - Journal and processor changes for the engine

- `Journal.openScope(event, actor, parentId)`: the parent becomes explicit, because a followup runs after its parent's
  scope has closed. Delete the `openScopes` stack. Only one scope is open at a time, so `JournalTest`'s nesting test
  goes too.
- `ScopeClosed(scopeId, followup)`: records what the event queued.
- New entry `ChoiceMade(scopeId, chooser, option)`: records the answer to a `OneOf`. The scope is the frame's parent.
- Split `EventProcessor.execute` into `prepare(game, event)` (context, `init()`, validation) and `run(ops)`
  (`process()`). This is what lets a rejection touch nothing. `EventTestHarness.execute` calls both, so processor tests
  don't change.
- The scope's `actor` is the player who submitted the event. For a queued event it is the event's player for a
  `PlayerEvent`, otherwise the acting player. Without this, a `SendToBrigEvent` answered by the Admiral would be logged
  under the current player.

## Phase 4 - Engine core

New in `engine/`: `GameEngine`, `Agenda`, `ProcessorRegistry`, `PendingInput`, `EngineStatus`.

- **`ProcessorRegistry`**: finds `XxxEventProcessor` by the naming convention, as `EventTestHarness` does, and creates a
  new instance per execution, because processors hold per-event fields.
- **`NoOpEvent`**: gets a trivial processor, so the run loop has no special case.
- **Input check**: the submitting player must match the pending input, and `decision.action().isInstance(event)` must
  hold, which also covers interface targets like `ActionEvent`. A submitted `PlayerEvent` must name that same player, or
  player 1 could submit a `DrawSkillCardsEvent` for player 3.
- **`status(player)`**: everyone gets the `PendingInput`, unless it is secret. Then only the player who has to answer
  gets it, and the others get a plain `Waiting`. Nothing is secret until phase 10, but the signature is there from the
  start.
- **Constraints**: a client must not be able to drop `DRAW_EXACTLY_2` by sending `drawLimit = null`. Add a
  `ConstrainedEvent` interface with `withConstraints(List<EventConstraint>)`, and have the engine overwrite whatever the
  client sent. `DrawSkillCardsEvent`, `DiscardSkillCardsEvent`, `LeaveBrigEvent` and `LaunchViperEvent` implement it.
  The last one maps `MUST_LAUNCH_IN_VIPER` onto its `mustLaunchInViper` boolean.
- **Errors**: input that fails the check or `isValid()` gets `InvalidActionException`, and the input stays pending. A
  queued event that fails validation is a bug: `FrakCallTheAdmiralException`. Until phase 5 adds the rollback, any
  exception after validation stops the game.
- **Events without a processor**: every concrete event has to have a processor, and `ProcessorRegistry.create` asserts
  it. The engine doesn't check for one, so an event without one is still under development, like `QuorumChamberEvent`'s
  `PlayQuorumCardEvent` option until Quorum cards exist.

A package-private constructor takes the first event, so phases 4 and 5 can test real events before `StartGameEvent`
exists.

Tests: `AgendaTest` for the ordering rules, `GameEngineTest` with a small script of real events covering decisions,
choices, rejected input and a clean journal after rejection.

## Phase 5 - Restore and rollback

- `GameEngine.restore(journal)` rebuilds `Game` through `GameReplayer`, then folds the entries through the same `Agenda`
  methods the run loop uses: `ScopeOpened` takes the top event, `ScopeClosed` pushes its followup, `ChoiceMade` selects
  the option. The first `ScopeOpened` finds an empty agenda, because `start` pushed that event.
- Rollback: `submit` and `choose` note the journal's size. If anything throws, the engine rebuilds itself from the
  entries before that point and rethrows. The facts hold every random result, so the rebuilt game needs none of the old
  randomness. The replay cost is only paid on failure. `Game` is a new object afterwards, so callers ask for `view()`
  again.
- Engine tests use a seeded `Randomness`, not `FakeDeck`, because restore rebuilds the real decks.
- Acceptance tests: at every pause of a scripted game, `restore(engine.journal())` gives the same `GameSnapshot` and the
  same `PendingInput`. An event that throws in the middle of a chain leaves both unchanged.

## Phase 6 - Turn loop

The first playable slice. New `event/turn/` events, each with a processor and test.

`StartGameEvent` follows the setup sequence in rule A3.1, minus what the Kobol objective doesn't use:

1. `ops.startGame()`, `ops.galactica().setUp()` and `CreateDestinyDeckEvent` (rules A3.4, A3.5). Nothing in main calls
   `setUp()` yet.
2. A random first player, recorded as a fact (rule A3.7.1).
3. `SelectCharacterEvent` decisions, clockwise from the first player (rule A3.7.2). A character with a choice of setup
   locations picks one through `PlaceInSetupLocationEvent`.
4. Titles through the `DetermineNext...` events, then the President draws a Quorum Card (rule A3.7.3).
5. `CreateLoyaltyDeckEvent` (rule A3.8).
6. Starting Skill Cards: 3 each, except the first player, who draws on their first turn, and Cylon Leaders, who draw 2
   (Athena draws 3) (rule A3.9).

The turn events:

- `StartTurnEvent`: records the turn and returns the steps as one `all(...)`.
- One event per step, recording `StepChanged`. Movement and Action are optional. The Action step offers a location
  action, `ActivateViperEvent` for a pilot (it isn't an `ActionEvent`), or doing nothing (rule 8.4).
- Each step applies its own exceptions. Revealed Cylons and characters in the Brig skip Crisis, and revealed Cylons skip
  Activate Cylon Ships (rules 8.5, 8.6). Prepare for Jump isn't skipped on their turn (rule 6.1.3.4), see phase 11.
- `DiscardStepEvent`: every player over 10 Skill Cards or 10 Quorum Cards discards down to 10, not only the current
  player (rule 8.8).
- `EndTurnEvent`: checks resources, passes the current player token, returns the next `StartTurnEvent`. This is the only
  resource check (rule 16.1). A resource can reach 0 during a turn, as FTL Control can with population, and recover
  before the turn ends.

Crisis is a stub here (draw the card, discard it) until phase 11. The acceptance test plays several full turns through
`submit` and `choose` alone.

**Stranded Helo.** His character sheet keeps him off the board until the start of his 2nd turn, when `StartTurnEvent`
places him on the Hangar Deck. While `game.locate` is empty he can't move, be moved or take actions, so the Movement and
Action steps skip him, and `SendToBrigEvent` and `SendToSickbayEvent` leave him alone, as
`ViolentOutburstsEventProcessor` already does. Finding his 2nd turn needs a count of his own turns, because
`Turn.number` counts everyone's. After an execution he is Stranded for his player's next turn (rule A2.1.1.10).

## Phase 7 - Validate player input

`isValid()` is the only rule check a submitted event gets, and only 22 of 95 processors have one. A bad submit now rolls
back instead of killing the game, but nothing stops a legal-looking one from cheating.

- **Location actions.** None of the 21 `ActionEvent` processors checks that the player is at the location or that it
  isn't damaged (rule 11.7.2.2), and 16 have no `isValid()` at all. Add `location()` to `ActionEvent` and a
  `LocationActionProcessor` base class with the shared checks. The board views don't expose damage yet. 18 of those
  processor tests never call `moveTo`, so their setup changes.
- **Decision targets.** 10 have no `isValid()`: `DamageHumanFighterEvent`, `DestroyCivilianShipEvent`,
  `Discard1MutinyCardEvent`, `DiscardDownTo1MutinyCardEvent`, `DogfightEvent`, `ExploitAWeaknessEvent`,
  `ForceTheirHandEvent`, `MakePresidentEvent`, `ProtectTheFleetEvent`, `SendToSickbayEvent`.
- **Decisions that lose their context.** `MainBatteriesEventProcessor` asks for a `DestroyCivilianShipEvent`, but the
  ship has to be in the targeted space area, and the event can't know that. Use `one(...)` with concrete events when the
  options are few, as `ActivateRaiderEventProcessor` does, and a constraint when they aren't (`DestroyRaidersEvent`
  takes a subset).
- **`LaunchViperEvent`** isn't a `PlayerEvent`, so its processor can't check the pilot against the deciding player. It
  has a `todo` for this. Making it a `PlayerEvent` closes that.
- **A decision must always have a legal answer**, or the game waits forever. A processor queues one only if an answer
  exists. `HangarDeckEventProcessor` queues a mandatory launch without checking that a viper is available, and
  `DrawSkillCardsEvent` has no answer when a deck and its discard pile are empty (rule 8.2.2.5).

## Phase 8 - End of game and Mutiny

**Win checks.** After `process()`, the run loop asks `WinChecks` (reads the views, returns a `Followup`). The result
goes on top of the event's own followup, and `ScopeClosed` records the combined followup, so restore never re-runs the
checks. It covers the immediate Cylon wins from rule 16.1: a centurion at the end of the Boarding Party track, and 6 or
more damaged Galactica locations. `ActivateHeavyRaidersAndCenturionsEventProcessor` already queues the first one. Remove
that, so the check lives in one place.

**End of game.** Add a `GameEnded(winner)` fact and `Game.outcome()`, and give `CylonsWinEvent` a processor. When the
outcome is set, the run loop clears the agenda and reports `Finished`.

**Second Mutiny Card.** This can't be a check on the state, even though the `todo` in `PlayerOps.drawMutinyCard()` asks
the engine to handle it. Rule 5.2.3.2 moves the player to the Brig unless told otherwise, and a check only sees two
cards. Press Room says otherwise, a Stranded Helo discards without moving (rule A2.1.1.10), and the Mutineer moves on
the third card (rule 5.2.3.3).

- Put it in `DrawMutinyCardEventProcessor`, including keeping one card while in the Brig.
- `ExploitAWeaknessEventProcessor`, `ForceTheirHandEventProcessor` and `PersonalVicesEventProcessor` return a
  `DrawMutinyCardEvent` instead of calling the op.
- `PressRoomEventProcessor` keeps calling the op, because it is the exception.

## Phase 9 - Skill check wiring

Roadmap step 1 isn't finished. The processors exist but aren't chained.

- Nothing queues `PoliticalProwessEvent`, `RevealCardsBeforeSkillCheckEvent`, `PlayCardsIntoSkillCheckEvent`,
  `ShuffleAndDivideCardsEvent`, `TotalSkillCheckStrengthEvent`, `DrawAndResolveRecklessCardsEvent` or
  `ResolveConsequencesEvent`.
- `EndSkillCheckEvent` from `skill-check.drawio` doesn't exist, and `activeSkillCheck` is never cleared.
- Only Admiral's Quarters starts a skill check. The Administration, Airlock and Brig locations have skill check types
  and results but no action event, and the Basestar Bridge has no action event either.

Acceptance test: a skill check runs from the action that starts it to `EndSkillCheckEvent` through `submit` and
`choose`.

## Phase 10 - Interrupt cards

Six skill cards are played in response to something, outside the Action step. All six are in `SkillCardType`, and none
has an event yet. They go in `event/interrupt/`.

| Card                 | Window                                    | Effect                                      |
|----------------------|-------------------------------------------|---------------------------------------------|
| `STRATEGIC_PLANNING` | Before a die roll                         | +2, one per roll                            |
| `CALCULATIONS`       | After a die roll                          | +1 or -1, one per roll                      |
| `EVASIVE_MANOEUVRES` | After a viper is attacked                 | Reroll, -2 if the viper is piloted          |
| `RUN_INTERFERENCE`   | Before raiders activate                   | The first 4 raiders in your area miss       |
| `BEST_OF_THE_BEST`   | Before a piloted viper attacks            | Roll, destroy that many raiders in its area |
| `MAJOR_VICTORY`      | After you destroy a basestar or centurion | Roll, 5 or more gains 1 morale, one a turn  |

**Windows.** `OpenInterruptWindowEvent(window)` is an ordinary queued event, so the engine needs no new input, and
`submit`, `choose`, restore and rollback work unchanged. Its processor runs when the window opens, so it sees the hands
as they are at that moment.

- It asks each human player holding a card for that window, one at a time, clockwise from the current player. The
  question is a plain decision on `PlayInterruptCardEvent`, and playing no card means passing, as in
  `PlayCardsBeforeSkillCheckEvent`. It isn't a `OneOf`, so there is only one kind of input to keep secret.
- Revealed Cylons can't use Skill Card abilities (rule 6.1.2.3), and Cylon Leaders count as revealed unless Infiltrating
  (rule 6.2.1). `isHuman()` covers both.
- Played cards wait until everyone has answered. If two conflict, the current player picks which one stands and the
  other returns to its owner's hand (rule 13.3). This is the same shape as the before-skill-check cards, with
  `RevealCardsBeforeSkillCheckEvent` and `TiebreakCardsBeforeSkillCheckEvent`.
- A window with no holders doesn't pause the game.

**Secrecy (decision 8).** `PlayInterruptCardEvent` implements a new `SecretEvent` marker, and `PendingInput` derives
`secret()` from the decision's action, so restore needs nothing extra. While a secret input is pending, `status(player)`
gives it only to the player who has to answer. A played card stays hidden until the window closes, and a pass is never
shown. A window with no holders doesn't pause, so a pause still shows that someone holds a card, just not who.

**Die rolls.** `ops.rollDie()` runs inside `process()`, so nothing can pause around it.

- Add `RollDieEvent` and a `FollowupUtils` helper that returns `all(window before, RollDieEvent, window after, result)`.
- The 8 processors that call `ops.rollDie()` split in two: the event queues the roll, and a new result event does what
  came after it.
- `Turn` holds the current `DieRoll`, because the result event runs after a pause. `DieRolled` starts changing state,
  where `FactApplier` ignores it today, and a new fact records modifiers. The window before a roll starts a new one.
- Modifiers survive a reroll (rule A2.1.1.10). The total is clamped to 1-8 after all of them (rule 13.1).
- An `ArchitectureTest` rule lets only `RollDieEventProcessor` call `GameOps.rollDie()`, so a new roll can't skip the
  windows.

**Attacks.** Four of the six cards hang on attacks that don't exist yet, which is why this phase comes before Crisis. It
adds processors for `AttackViperEvent`, `AttackRaiderEvent`, `AttackHeavyRaiderEvent`, `AttackBasestarEvent`,
`AttackGalacticaEvent`, `ActivateViperEvent` and `DestroyRaidersEvent`, with the windows in from the start. Cylon
activations and Weapons Control already queue some of them. `RUN_INTERFERENCE` needs a counter that lasts for one raider
activation.

Acceptance tests: a held card pauses the game at its window, a window with no holders doesn't, the other players get a
plain `Waiting` during a window, two `STRATEGIC_PLANNING` cards on one roll go to the current player, and restore works
at a pause inside a window.

## Phase 11 - Crisis

`ResolveCrisisCardEvent` and `CrisisCardFollowups` resolve 16 choice crises, and their `CrisisCard` constants list the
chooser and both icons. The other constants are still bare. The remaining 28 event crises need their effects, and the
skill check crises need their skill check type. Some of those let a title holder pick an alternate instruction instead
of the skill check (rule 8.5.1.1).

- The Crisis step and `ResolveCapricaCrisisCardEvent` queue `ResolveCrisisCardEvent`.
- `Turn` keeps the Crisis Cards drawn this turn, through a new fact, and `TurnView` exposes them. The Activate Cylon
  Ships step, the Prepare for Jump step and the discard in `EndTurnEvent` (rule 8.8) read them.
- A card chosen at Caprica counts for Prepare for Jump even on a revealed Cylon's turn (rule 6.1.3.4), so that step
  can't depend on the Crisis step having run.

## Phase 12 - Jump and Kobol

- `JumpingTheFleetEvent` has no processor, and no code tracks distance. That blocks the Sleeper Agent phase at 4
  distance and the end game after 8 (rule 12.4), including the unrevealed Personal Goal check (rule 16.2).
- `GameEnded` names the winning team. A Cylon Leader's own result comes from their Motive Cards (rules 2.3, 16.3).
- `ExecuteEvent`'s processor covers the loss when no characters are left to select (rule 2.2). It depends on the
  execution, so it isn't a win check.
- 39 event records have no processor today: the 3 placeholders, `NoOpEvent` and 35 real events. Phases 8 to 12 add the
  ones they reach.

Acceptance test: seeded games played to a Cylon win and to the Kobol end game, restoring at every pause.

## Phase 13 - Docs

- Update `CLAUDE.md`: packages, architecture, roadmap. `Turn` moves from the root package description to `state/`.
- Replace the diagram in `README.md`, and add this plan to "Design Documents".

## Not in this plan

- Executive Order and the other `Action:` and `Movement:` skill cards, Cylon reveal, Quorum and Super Crisis cards. They
  plug in as new events.
- Interrupts that aren't skill cards: character abilities such as Helo's reroll, and revealing a Motive Card in the
  middle of resolving something (rule 6.2.2.1). They can reuse the windows from phase 10.
- Describers, the rest of per-viewer hiding, JSON serialization, and the frontend. Describers have to keep a window's
  answers hidden too, because the journal records them in full.
- Concurrency. Callers serialize calls per game.
