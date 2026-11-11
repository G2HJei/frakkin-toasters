# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Web version of the Battlestar Galactica board game. Java implementation modeling the game's state, events, and
mechanics. So far only the game-logic `core` module exists. There is no engine driving turns yet and no frontend.

## Scope

The rules and card images are in `materials/` (`bsg-rules/Battlestar_Galactica_CR2-0.pdf` and its Appendix are the
combined rulebook v2). The first version covers the full game with the Kobol objective.

Out of scope: everything that only applies with the New Caprica, Ionian Nebula or Earth Objective Cards. The rulebook
marks these rules with a blue, green or orange background. Don't model them, and remove them if you find them in the
code. That includes:

- New Caprica: the New Caprica board and its locations (Detention, Resistance HQ, ...), New Caprica crisis cards and
  occupation forces
- Ionian Nebula: trauma tokens, allies, crossroads cards and the alternate basestar damage tokens
- Earth: the Demetrius and Rebel Basestar boards, mission cards, the basestar allegiance marker and the Hub Destroyed
  overlay

## Build Commands

```bash
./gradlew build          # Build the project
./gradlew test           # Run all tests
./gradlew :core:test     # Run tests in the core module
./gradlew :core:test --tests "xyz.zlatanov.frakkintoasters.event.player.MoveEventProcessorTest"  # Run a single test class
```

## Design Documents

- `skill-check.drawio` - the full skill check sequence: before-check cards, playing cards in, abilities, result,
  consequences, and the optional followup events.
- `architecture.md` - the target design for the `GameView` + game ops + journal + describers + engine and the journal.

## Roadmap

Next steps, in order:

1. Complete the skill check sequence according to `skill-check.drawio`.
2. ~~Reanalyze existing events: `PlayerEvent` or current player.~~ Done. The engine still has to call
   `GameOps.actingPlayer`
   for "Executive Order".
3. ~~Create the journal and tie it to the ops classes in `operation/`.~~ Done, see "Journal and Replay". Still open:
   describers and per-viewer hiding, and JSON serialization. The pending followups are the engine's `Agenda`.
4. Create the game engine to drive the events and turns. `engine-plan.md` has the plan in phases. Phases 1 to 8 are
   done: the journal moved out of `Game` into `engine/journal/`, the turn state is grouped in `state/Turn`, the journal
   and processors are ready for the engine, `GameEngine` runs events through `submit` and `choose`, it restores a
   game from its journal and rolls back a failed input (see "Engine"), `GameEngine.start` sets up a game and plays
   its turns (see "Turn Loop"), processors check what players submit (see "Validation"), and the Cylons can win (see
   "End of Game and Mutiny"). Next is phase 9: skill check wiring.
5. Create a working frontend with the events that exist by then.

## Project Structure

Multi-module Gradle project with a single `core` module. Package root: `xyz.zlatanov.frakkintoasters`.

### Key Packages

- root - `EventProcessor` (base class for all processors)
- `event/` - event records and their processors, grouped by area (`action`, `board`, `crisis`, `deck`, `endgame`,
  `location/{galactica,pegasus,colonialone,cylon}`, `loyalty`, `officials`, `player`, `ship`, `skillcheck`,
  `skillcheck/ability`, `skillcheck/result`, `turn`). Also `placeholder/` (events that need player input) and
  `constraint/` (`EventConstraint`, `ConstrainedEvent`). `location/` holds `LocationActionProcessor`, the base of the
  location action processors
- `operation/` - game ops, the only code that decides what changes: `GameOps` (entry point) hands out `PlayerOps`,
  `ShipOps`, `GalacticaOps`, `PegasusOps`, `CylonFleetOps`, `SkillCheckOps`, `LoyaltyDeckOps`, `TitleOps`. They extend
  `AbstractOps`, which holds the `Game` and a `FactRecorder`, and offers `record`, `draw`, `shuffle`. Ops record facts,
  they never write to components
- `engine/` - `GameEngine` (runs a game, see "Engine"), `Agenda` (stack of pending followups), `AgendaReplayer`
  (rebuilds the agenda from a journal), `ProcessorRegistry` (maps each event class to its processor, scanned once from
  the classpath), `PendingInput` and `EngineStatus` (what the game waits for), `ScopeActor` (who a journal scope is
  logged under)
- `engine/journal/` - `Journal`, `JournalEntry`, `FactRecorder`, `FactApplier`, `ShipZones`, `GameReplayer`, and `fact/`
  with the `Fact` records
- `state/` - game state model: `Game` (top level), `Turn` (turn number, `GameStep`, current and acting player, the
  Crisis Cards of the turn, read through `GameView.turn()`; `number` counts player turns, not rounds), `Player` (`turns()` counts the player's own
  turns), `Team` (the winner in `GameView.outcome()`), `Die` (d8), `Randomness`,
  `RemovedComponents`, `PlayerPile`, and the `@Mutates` marker
- `state/view/` - read-only view interfaces (`GameView`, `TurnView`, `PlayerView`, `BoardsHolderView`,
  `BattlestarBoardView` (damaged locations), `DecksHolderView` (Skill Cards left to draw), `SkillCheckView`, ...)
  implemented by the state classes. Processors read state only through these
- `state/board/` - board hierarchy: `Board` (base, tracks characters by location) -> `BattlestarBoard` (adds damage
  tracking) -> `GalacticaBoard`/`PegasusBoard`. Also `CylonFleetBoard`, `SpaceLocationsBoard`, `BoardsHolder`
  (aggregates
  all boards). The `Location` enum defines all 35 board spaces in scope, Colonial One as in Daybreak
- `state/ship/` - `Ship` interface with implementations: `Viper`, `ViperMarkVII`, `Raptor`, `AssaultRaptor`,
  `Basestar`, `Raider`, `HeavyRaider`, `Centurion`, `CivilianShip`. `CylonShips` manages the Cylon ship supply. Ships
  have unique integer IDs
- `state/deck/` - generic `Deck<T>` (draw/discard/shuffle). `DecksHolder` manages all game decks and routes discards to
  the correct deck
- `state/skill/` - `SkillCard` (record), `SkillCardType`, `SkillCardColor` (6 colors), `SkillCheck` (active check with
  its card piles), `SkillCheckType`, `PlayedCard`
- `state/card/` - card enums: `LoyaltyCard`, `QuorumCard`, `MutinyCard`, `ObjectiveCard`, `DestinationCard`,
  `MotiveCard`
- `state/crisis/` - `CrisisCard` (with its printed `CrisisChooser` and `CylonActivation`), `SuperCrisisCard`
- `state/damage/` - damage card enums: `GalacticaDamage`, `PegasusDamage`, `BasestarDamage`
- `state/track/` - `BoardingParty`, `JumpPreparation`, `Pursuit`
- `state/character/` - `Character` enum (33 BSG characters), `CharacterType` enum
- `state/exception/` - `InvalidActionException` (an event failed validation) and `FrakCallTheAdmiralException` (should
  never happen, i.e. a programming error)

## Architecture

### Events and Processors

Events are plain records with no logic. `Event` is an empty marker interface. `PlayerEvent` adds `playerNumber()`.
`ActionEvent extends PlayerEvent` marks an action a player takes in the Action step. `LocationEvent extends ActionEvent`
marks the action of a location and names it with `location()`. Its static `canAct` says whether a player can take the
action of a location at all (see "Validation").

A `PlayerEvent` is for events whose player can differ from the current player (an "Executive Order" target, any player
in a
skill check, targets of an effect). An event that always involves the current player is a plain `Event` and reads
`GameView.currentPlayer()`. `GameView.actingPlayer()` is the player taking an action or moving right now: the current
player, unless another one acts during that turn (set it through `GameOps.actingPlayer(n)` and `resetActingPlayer()`).
It never changes who the "current player" is for game effects such as skill checks.

Each `XxxEvent` has an `XxxEventProcessor extends EventProcessor<XxxEvent>` in the same package. Keep that naming:
`ProcessorRegistry` and `EventTestHarness` find the processor by it. A processor runs one event, in two calls:

1. `prepare(GameView, event)` sets the context (`game`, `event`, and helper views `galacticaBoard`, `pegasusBoard`,
   `cylonFleetBoard`, plus `player` when the event is a `PlayerEvent`), calls `init()` (optional setup, marked for
   removal), then `isValid()`, and throws `InvalidActionException` if it returns false. It returns the processor.
2. `run(GameOps)` sets `ops` and calls `process()`, which mutates state through `ops` and returns a `Followup`. It
   throws `FrakCallTheAdmiralException` unless `prepare` accepted the event, and on a second call.

`ops` is only set by `run`, so `init()` and `isValid()` can't change state, and the caller can validate before it opens
the event's journal scope. The caller builds the ops, so a processor never sees the concrete `Game` or the journal.
Processors read through `GameView` and write only through
game ops. Game ops do mechanics, and processors make decisions (targets, rules branching, followups).

Every state change leaves a fact in the journal, see "Journal and Replay".

Placeholder events (`event/placeholder/`: `PlayerDecisionEvent`, `PlayQuorumCardEvent`, `PlaySuperCrisisCardEvent`)
have no processor. They stand for a point where a player has to choose. `PlayerDecisionEvent<T>(playerNumber,
Class<T> action, List<EventConstraint>)` means "this player picks a concrete `T` event".

`FollowupUtils` builds shared followup sequences, e.g. `beforeSkillCheckFollowups(game)`, and holds the helpers that
`SkillCheckResultFollowups` and `CrisisCardFollowups` share, e.g. `lose`, `gain` and `discard`. `playersFrom(game, n)`
lists the players clockwise from player `n`, which is the order of the player numbers.

### Skill Check Results

`DetermineSkillCheckResultEventProcessor` queues a `ResolveSkillCheckResultEvent` after the Change of Plans decisions.
Its processor returns `NONE` when Change of Plans replaced the pass result or Iron Will prevented the fail result.
Otherwise it asks `SkillCheckResultFollowups` (in `event/skillcheck`) for the pass, partial pass or fail followup of
the active `SkillCheckType`, as printed on the card. The helper resolves the players (current player, President,
Admiral, the character chosen by a location action via `SkillCheckView.targetPlayerNumber()`) and board state at that
moment. Its pass and fail switches list every type, so a new `SkillCheckType` doesn't compile until it's mapped.

- An effect shared by 2 or more skill checks uses a generic event: an existing one when it fits (e.g.
  `DamageGalacticaEvent`, `SendToSickbayEvent`), otherwise one in its area package (e.g. `LoseResourceEvent`).
- An effect unique to one skill check uses a specialized event in `event/skillcheck/result`, named after the skill
  check and result (e.g. `CrashLandingFailEvent`), with the card text in its comment.

### Crisis Cards

`ResolveCrisisCardEvent(card)` resolves a Crisis Card. Nothing queues it yet: until phase 11 of `engine-plan.md`, the
Crisis step only draws a card and keeps it with the turn (see "Turn Loop"). So far it covers 16 choice crises (rule
8.5.2), and any other card throws `FrakCallTheAdmiralException`.

A `CrisisCard` constant lists what is printed on the card: its `CrisisChooser`, its `CylonActivation` icon and whether
it
has the "prepare for jump" icon. `CrisisCardFollowups` (in `event/crisis`) holds the effects, grouped like
`SkillCheckResultFollowups`:

- `choice(card)` gives a `OneOf` of two `ChooseCrisisSectionEvent`s, one per section. They are `PlayerEvent`s that name
  the chooser, because the engine derives who answers a `OneOf` from its options (decision 7 in `engine-plan.md`) and
  most sections name no player. Don't put the effects of a section directly into the `one(...)`.
- `followup(card, section)` gives the effects of the picked section.
- An effect on the current player goes through `onCurrentPlayer`, because a revealed Cylon resolving the card at
  "Caprica" ignores it (rule 6.1.2.1).

### Followups

Followups chain events using a recursive sealed interface with four variants:

- `Followup.None` - no further events (singleton `Followup.NONE`)
- `Followup.Single(Event)` - wraps a single event (leaf node)
- `Followup.AllOf(List<Followup>)` - execute all children in order (sequence)
- `Followup.OneOf(Set<Followup>)` - the player picks exactly one (choice). Backed by a `LinkedHashSet`, so order is kept
  and duplicates collapse

Factory methods: `single(event)`, `all(...)`, `one(...)`. They collapse trivial cases: no arguments gives `NONE`, one
argument gives that argument. `all(...)` and `one(...)` take either plain `Event` varargs (wrapped in `Single`) or
nested `Followup` varargs, so you can write "choose between a single event or a sequence of events":

```java
return one(
        single(new PlaySuperCrisisCardEvent(playerNumber)),

all(new DrawCrisisCardsEvent(playerNumber),
                new

ResolveCapricaCrisisEvent(playerNumber)));
```

### Event Constraints

`EventConstraint` is a plain enum of extra rules that can be pinned onto an event (e.g. `DRAW_EXACTLY_2`,
`DISCARD_EXACTLY_4`, `DISCARD_DOWN_TO_HAND_LIMIT`, `MUST_LAUNCH_IN_VIPER`, `DESTINATION_GALACTICA`), and
`drawExactly(n)` and `discardExactly(n)` give the counted ones. A processor attaches them to a
`PlayerDecisionEvent`, e.g. `new PlayerDecisionEvent<>(n, DrawSkillCardsEvent.class, DRAW_EXACTLY_2)`. When the
decision is answered, the engine copies them onto the submitted event with `ConstrainedEvent.withConstraints`,
replacing whatever the client sent, so a client can't drop one.

An event that supports a constraint implements `ConstrainedEvent` and has its own record component for it (e.g.
`DrawSkillCardsEvent.drawLimit`, `null` when there is none, or `LaunchViperEvent.mustLaunchInViper`). Its processor
checks the constraint in `isValid()` and throws `FrakCallTheAdmiralException` for any constraint it doesn't support.
`DrawSkillCardsEvent`, `DiscardSkillCardsEvent`, `DiscardQuorumCardsEvent`, `LeaveBrigEvent` and `LaunchViperEvent`
implement it.

### Validation

`isValid()` is the only rule check a submitted event gets, so a processor checks everything the client sent: that the
players, ships and cards it names exist and are where they have to be, and the rules of the action. A queued event goes
through the same check, and failing it there is a bug. So a rule that only binds a player's choice can't go in
`isValid()` when the event is also queued directly. The processor that asks offers the legal options as a `one(...)`
instead, e.g. `FollowupUtils.chooseCharacterForSickbay` (rule 9.3.2).

- Location action processors extend `LocationActionProcessor`. Its final `isValid()` checks that the player is at
  `event.location()` and that `LocationEvent.canAct` allows the action, then calls `isValidAction()` for the details
  of the event. `LocationEvent.canAct` is an exhaustive switch over `Location`, so a new location doesn't compile until
  it's mapped. A damaged location has no action (rule 11.7.2.2), and a location with preconditions asks the static
  `canAct` of its processor, e.g. FTL Control out of the red zone.
- A rule that another processor or a followup needs too is a public static check on its processor, e.g.
  `SendToBrigEventProcessor.canBeSentToBrig` or `MakePresidentEventProcessor.canBecomePresident`.
- `EventProcessor.isPlayer(n)` checks a player number from the client, and `holdsSkillCards(cards)` the hand. It counts
  copies, because the decks hold equal cards.
- A decision must always have a legal answer, or the game waits forever. A processor queues one only when an answer
  exists, and an optional one goes in a `one(decision, NoOpEvent)`.
- A Skill Card color with no cards in its deck and discard pile can't be drawn (rule 8.2.2.5,
  `DecksHolderView.skillCardsLeft`). `DrawSkillCardsEvent` may draw fewer than its `DRAW_EXACTLY_n` only when no other
  card fits, and fixed-color draws take what is left.
- Clients may send null fields, so a check that would throw on one rejects it instead.

### Engine

`GameEngine` runs a game. `submit(player, event)` answers the pending `PlayerDecisionEvent` with a concrete event, and
`choose(player, option)` picks an option of the pending `OneOf`. Both run the game forward until a player has to act
again, so `status(player)` is always `Waiting(PendingInput)` or `Finished(winner)`. It holds no game rules: processors
decide.
`GameEngine.start(playerCount, randomness)` begins a game with `StartGameEvent` (see "Turn Loop"). A package-private
constructor takes any first event, and `GameEngineTest` uses the test-only `ScriptEvent` for it.

- `Agenda` is a stack of `(followup, parentScope)` frames, so followups run depth first. Pushing flattens a followup:
  `None` adds nothing and `AllOf` pushes its children in reverse, so every frame is a `Single` or a `OneOf`.
- The run loop takes the event on top, gets a new processor from `ProcessorRegistry`, `prepare`s it, opens its scope,
  runs it, puts the result of `WinChecks` before the followup, closes the scope with both and pushes them. It stops at a
  `PlayerDecisionEvent` or a `OneOf`, and clears the agenda once the game has an outcome. Only that empties the
  agenda: running out of events before the game ends throws `FrakCallTheAdmiralException`.
- The chooser of a `OneOf` is the player of the `PlayerEvent`s in its options, otherwise the current player (decision 7
  in `engine-plan.md`). Options that name different players are a bug.
- Input check: the right player, `decision.action().isInstance(event)` (so `ActionEvent` takes any location action),
  and a submitted `PlayerEvent` names the submitter.
- Rejected input throws `InvalidActionException` and changes nothing. A queued event that fails validation and a
  decision with constraints its event can't carry throw `FrakCallTheAdmiralException`. Every concrete event class has to
  have a processor, and `ProcessorRegistry.create` asserts it. The engine doesn't check for one, so the events without
  one (e.g. `PlayQuorumCardEvent`) are still under development.
- Rollback: anything thrown after an input was accepted rolls the call back, and the engine rethrows it as a
  `FrakCallTheAdmiralException`. `advance` notes the journal's size, and on failure `rebuild`s the engine from the
  entries before it. The same input stays pending. `view()` and `journal()` return new objects
  afterwards, so callers ask again.
- `GameEngine.restore(journal)` rebuilds a game from a copy of the journal: `GameReplayer` applies the facts and
  `AgendaReplayer` folds the scope entries through the `Agenda` moves of the run loop (`ScopeOpened` takes the top
  event, `ScopeClosed` pushes its followup, `ChoiceMade` selects the option, and the scope with a `GameEnded` fact
  clears the agenda when it closes). The first event's `ScopeOpened` finds an empty agenda. A journal that doesn't match
  those moves, or ends neither at a pause nor with the end of the game, throws `IllegalArgumentException`.

### Turn Loop

The setup and the turns are ordinary events in `event/turn/`, so the engine knows nothing about steps. Each is a plain
`Event` about the current player.

- `StartGameEvent` follows rule A3.1 for the Kobol objective: `ops.startGame()`, `ops.galactica().setUp()`, a random
  first player (`GameOps.chooseFirstPlayer`), then queues `CreateDestinyDeckEvent`, the `SelectCharacterEvent`
  decisions clockwise from the first player, the three `DetermineNext...` events, `CreateLoyaltyDeckEvent`,
  `ReceiveStartingSkillsEvent` (everyone but the first player) and the first `StartTurnEvent`.
- `StartTurnEvent` records the turn (`GameOps.startTurn()` counts it for the game and the current player) and queues
  one event per step, then `EndTurnEvent`: `ReceiveSkillsStepEvent`, `MovementStepEvent`, `ActionStepEvent`,
  `CrisisStepEvent`, `ActivateCylonShipsStepEvent`, `PrepareForJumpStepEvent`, `DiscardStepEvent`.
- Each step records `StepChanged` and applies its own exceptions. A step a player doesn't have records nothing: revealed
  Cylons (and Cylon Leaders who aren't Infiltrating, see `isHuman()`) have no Crisis and no Activate Cylon Ships step,
  and characters in the "Brig" no Crisis step. Movement and Action are a `OneOf` with a `NoOpEvent`. A pilot gets
  `ActivateViperEvent` instead of a location action, and a character whose location has no action he can take (e.g. in
  "Sickbay", see `LocationEvent.canAct`) gets no Action choice.
- `DiscardStepEvent` asks every player over the hand limit, clockwise from the current player, with
  `DISCARD_DOWN_TO_HAND_LIMIT`: Skill Cards over `handLimit()`, and the President's Quorum Cards.
- The Crisis Cards of a turn stay with it, in `TurnView.crisisCards()` (`GameOps.playCrisisCard`), for the icons the
  later steps read. `EndTurnEvent` discards them (rule 8.8).
- `PrepareForJumpStepEvent` happens only when a Crisis Card of the turn has the "prepare for jump" icon, because with
  the Kobol objective the icon is all the step does. It queues an `IncreaseJumpPreparationEvent` per icon, and the
  Mutineer draws a Mutiny Card (rule 8.7.1). `IncreaseJumpPreparationEvent` queues `JumpingTheFleetEvent` when the
  token moves past `MINUS_1`: the track has no "Auto Jump" space, the token goes back to `START`.
- `EndTurnEvent` is the only resource check (rule 16.1). It queues `CylonsWinEvent` at 0, otherwise it passes the
  current player token to the left and queues the next `StartTurnEvent`.
- Stubs until phase 11: the Crisis step doesn't resolve its card, and the Activate Cylon Ships step does nothing.
- A Stranded Karl "Helo" Agathon is off the board until `StartTurnEvent` puts him on the Hangar Deck at his player's
  2nd turn (`PlayerView.turns()`). Until then the Movement and Action steps skip him, and `SendToBrigEvent` and
  `SendToSickbayEvent` leave him alone.

### End of Game and Mutiny

- `WinChecks` (`event/endgame/`) holds the Cylon wins that happen at once (rule 16.1): a centurion at the end of the
  Boarding Party track, and 6 damaged Galactica locations. The engine asks after every event, and `ScopeClosed` records
  the result with the event's followup, so restore never runs the checks. The resource check stays in `EndTurnEvent`.
- `CylonsWinEvent` records `GameEnded(winner)` (`GameOps.endGame`), and `GameView.outcome()` gives the winning `Team`,
  which `Finished(winner)` reports. A test script that plays to the end has to end the game, e.g. with a
  `CylonsWinEvent`.
- To draw a Mutiny Card, queue a `DrawMutinyCardEvent`. Its processor sends the player to the "Brig" on the second card
  (the Mutineer on the third, `PlayerView.isMutineer()`), and asks for a `Discard1MutinyCardEvent` per card over the
  limit (rules 5.2.3.2 and 5.2.3.3). In the "Brig" and off the board (a Stranded Helo) the player only discards. Only
  the Press Room calls `PlayerOps.drawMutinyCard()`, because its target doesn't move.

### Journal and Replay

The journal records exactly what the game ops did, so a game can be rebuilt from the journal alone (`journal-plan.md`
has
the design). It is fact-sourced: replay applies recorded facts, and never re-runs processors or needs randomness.

- `Fact` (`engine/journal/fact/`) is a sealed interface, grouped by area: `BoardFact`, `DeckFact`, `GameFact`,
  `PlayerFact`, `ShipFact`, `SkillCheckFact`, `TitleFact`. The records are nested in the area interfaces, because a
  sealed hierarchy can't span packages without a `module-info`. A new fact has to be handled in `FactApplier`'s
  exhaustive `switch`.
- `Journal` is an append-only flat list of `ScopeOpened(scopeId, parentId, event, actor)` / `FactRecorded` /
  `ScopeClosed(scopeId, followup)` / `ChoiceMade(scopeId, chooser, option)`. It isn't part of `Game`: `GameEngine`
  owns it, and outside the engine whoever builds the ops does (`TestHarness`, `GameReplayerTest`). Ops never see it,
  they get a `FactRecorder(game, journal)`.
- At most one scope is open at a time. A followup runs after its parent's scope has closed, so
  `openScope(event, actor, parentId)` takes the parent explicitly, and `closeScope(followup)` records what the event
  queued. `recordChoice` logs the answer to a `OneOf` between events, under the scope that queued it. Facts recorded
  while no scope is open go under `Journal.ROOT_SCOPE`. `GameEngine` opens a scope per event after `prepare`, and so
  does `EventTestHarness.execute`.
- The scope's `actor` is the player who submitted the event. For a queued event, `ScopeActor.ofQueued` (`engine/`)
  gives the event's player for a `PlayerEvent`, otherwise the acting player.
- `FactApplier` is the only code that changes components. `FactRecorder.record(fact)`, which `AbstractOps.record` calls,
  applies a fact and then appends it, so live play and replay share one code path. `ShipZones` (same package) moves
  ships between zones by id.
- `GameReplayer.replay(journal)` builds the game from the first fact, `GameStarted`, and applies the rest.

Rules for facts and ops:

1. **A fact applies exactly what it says and nothing more.** No cascades: `LocationDamaged` marks the damage, and the
   Sickbay moves are separate `CharacterMoved` facts logged after it.
2. **Every random or looked-up result goes into the fact**: drawn cards, the order after a shuffle, die values, ship
   ids.
   Randomness comes from `Game.randomness()` (`Randomness`, seedable), never from `Collections.shuffle` or
   `new Random()`.
3. **Ops never write to components.** They compute a fact from the current state and call `record(fact)`. Drawing is
   `draw(DeckId)` (peek, record `CardsDrawn`, log the reshuffle when the draw empties a reshuffling deck), shuffling is
   `shuffle(DeckId)`. A drawn card is in no pile until another fact says where it went.
4. **Fact fields are complete and immutable.** Cards are listed in full. Hiding them is the describers' job (not built
   yet).
5. **`@Mutates`** marks every component method that changes state. `ArchitectureTest` (ArchUnit) allows calls only from
   `..journal..` and `..state..`, and checks that `..event..` and `..operation..` never call them. Tests are not
   analyzed. Mutable collections that getters expose (e.g. `Board.characters()`) can't be marked, don't change state
   through them. Two more rules keep the layers apart: `..state..` depends on none of `..operation..`, `..event..` and
   `..engine..`, and `..event..` doesn't depend on `..engine..`.
6. Ships and centurions are named by their fixed ids. Decks are built in canonical order and shuffled by
   `GameOps.startGame()`.

### Lombok Usage

Uses Lombok extensively: `@Builder`, `@Getter`, `@Accessors(fluent = true)`, `@RequiredArgsConstructor`, `val`.
Accessors are fluent style (no `get`/`set` prefix), e.g. `game.players()` not `game.getPlayers()`. Prefer Lombok `val`
when possible.

### Testing Patterns

- JUnit 5 + Mockito
- `TestHarness` - base class that builds a `Game` with fake components and exposes the boards, decks, ships, the
  `journal` and `recorder`, and an `ops` instance bound to them. Game op tests (`operation/`, e.g.
  `PlayerOpsDrawSkillCardTest`) extend it directly
- `EventTestHarness<E>` - base class for processor tests. Name the test `XxxEventProcessorTest` in the processor's
  package and the harness creates the processor by reflection. Use `execute(event)`, `assertFollowup(...)`,
  `assertInvalid(event)`, `assertSkillCheckTriggered(type)`. If a test doesn't assert a followup, the harness checks
  that it was `Followup.NONE`. Players start without a character: a location action test gives one and `moveTo`s the
  location first. A character can be set only once
- `FakeDie` - test double for `Die` with `nextRoll(int)` to control randomness
- `FakeDeck<T>` - test double for `Deck<T>` with `nextCard(T...)` to control draws, and `clear()` to empty the draw
  pile
- `GameSnapshot` (`engine/journal/`) - turns a `Game` into comparable plain values. Replay tests compare a live game
  with its replayed twin. `GameReplayerTest` has the scripted scenario that has to record every kind of fact
- Op tests also assert the logged facts (`journal.facts()`), and `FactApplierTest` covers each fact
- `GameEngineTest` plays a script of real events through `submit` and `choose`, started by the test-only `ScriptEvent`
  (it calls `ops.startGame()` and queues the script). The registry finds test processors such as
  `ScriptEventProcessor` too, because it scans the whole classpath. At every pause, `restore` has to give the same
  `GameSnapshot` and statuses. The test-only `FailingEvent` changes the game and then throws, to test rollback. Engine
  tests use a seeded `Randomness`, not `FakeDeck`, because restore rebuilds the real decks
- `TurnLoopTest` plays a seeded game from `GameEngine.start` through several full turns, restoring at every pause. Its
  inputs are relative to the first player the seed picks
- Tests follow the pattern: set up game state, execute the event, assert state changes and followups. Use real game ops
  in processor tests, not mocks, since processors may read their own changes
