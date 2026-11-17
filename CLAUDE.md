# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Web version of the Battlestar Galactica board game. Java implementation modeling the game's state, events, and
mechanics. The game-logic `core` module holds the rules and the engine, and the Spring Boot `backend` module serves
games to browsers (see `backend-plan.md`), and the React `frontend` module is the website and the table (see
`frontend-plan.md`).

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
./gradlew :backend:test  # Run tests in the backend module (Testcontainers: Docker has to run)
docker compose up -d     # Postgres for local runs, on port 5433
./gradlew :backend:bootRun --args='--spring.profiles.active=dev'  # builds the frontend and serves the whole app on 8080
```

## Design Documents

- `skill-check.drawio` - the full skill check sequence: before-check cards, playing cards in, abilities, result,
  consequences, and the optional followup events.
- `backend.drawio` - the backend: logins, lobby, the live connections, `GameSessions` and the tables.

## Roadmap

Next steps, in order:

1. ~~Complete the skill check sequence according to `skill-check.drawio`.~~ Done, see "Skill Check Sequence".
2. ~~Reanalyze existing events: `PlayerEvent` or current player.~~ Done. The engine still has to call
   `GameOps.actingPlayer`
   for "Executive Order".
3. ~~Create the journal and tie it to the ops classes in `operation/`.~~ Done, see "Journal and Replay". Still open:
   Done since: per-viewer hiding (`projection/`, see "Projections") and JSON (the backend's `JournalCodec`). The pending
   followups are the engine's `Agenda`.
4. ~~Create the game engine to drive the events and turns.~~ Done: the journal lives in `engine/journal/`, the turn state is grouped in `state/Turn`, `GameEngine` runs events through `submit` and `choose`, it restores a
   game from its journal and rolls back a failed input (see "Engine"), `GameEngine.start` sets up a game and plays
   its turns (see "Turn Loop"), processors check what players submit (see "Validation"), the Cylons can win (see
   "End of Game and Mutiny"), a skill check runs from the action that starts it to its end, with every location
   action in scope (see "Skill Check Sequence" and "Execution"), and the six interrupt Skill Cards are played in
   secret windows around die rolls and attacks (see "Interrupt Cards" and "Combat"), and the Crisis step resolves every
   Crisis Card and activates the Cylon ships by its icon (see "Crisis Cards"), and the fleet jumps through the Sleeper Agent
   phase to the Kobol end game (see "Jump and Kobol"). Still open, as new events that plug in: Executive
   Order and the other `Action:` and `Movement:` skill cards, Cylon reveal, Quorum and Super Crisis cards, and the
   interrupts that aren't skill cards (character abilities such as Helo's reroll, and revealing a Motive Card in the
   middle of resolving something, rule 6.2.2.1), which can reuse the interrupt windows.
5. ~~Create a working frontend with the events that exist by then.~~ Done (`backend-plan.md`, `frontend-plan.md`):
   the backend, the sandbox, the wire schema, the table with the board, the lobby and rooms (see "Backend" and
   "Frontend"). Still open: legal-move hints (a list in `core` of the legal answers to a decision), more inputs on the
   board (viper activation, targets of a choice lighting up), and the events still open in the engine.

## Project Structure

Multi-module Gradle project on Java 21: `core` (no libraries but Lombok), `backend` (Spring Boot 4, package
`xyz.zlatanov.frakkintoasters.backend`) and `frontend` (React, built through node-gradle). Package root: `xyz.zlatanov.frakkintoasters`. The processor registry scans the
whole root package, so nothing in `backend` may be named `XxxEventProcessor`.

### Key Packages

- root - `EventProcessor` (base class for all processors)
- `event/` - event records and their processors, grouped by area (`action`, `board`, `crisis`, `deck`, `endgame`,
  `interrupt`, `jump`, `location/{galactica,pegasus,colonialone,cylon}`, `loyalty`, `officials`, `player`, `ship`,
  `skillcheck`, `skillcheck/ability`, `skillcheck/result`, `turn`). The attacks and the viper activation are in the root
  package. Also `placeholder/` (events that need player input) and
  `constraint/` (`EventConstraint`, `ConstrainedEvent`). `location/` holds `LocationActionProcessor`, the base of the
  location action processors
- `operation/` - game ops, the only code that decides what changes: `GameOps` (entry point) hands out `PlayerOps`,
  `ShipOps`, `GalacticaOps`, `PegasusOps`, `CylonFleetOps`, `SkillCheckOps`, `LoyaltyDeckOps`, `TitleOps`,
  `InterruptOps`. They extend
  `AbstractOps`, which holds the `Game` and a `FactRecorder`, and offers `record`, `draw`, `shuffle`. Ops record facts,
  they never write to components
- `engine/` - `GameEngine` (runs a game, see "Engine"), `Agenda` (stack of pending followups), `AgendaReplayer`
  (rebuilds the agenda from a journal), `ProcessorRegistry` (maps each event class to its processor) and
  `EventCatalog` (every event type, records and interfaces), both scanned once from the classpath by
  `ClasspathScanner`, `PendingInput` and `EngineStatus` (what the game waits for), `ScopeActor` (who a journal scope is
  logged under)
- `engine/journal/` - `Journal`, `JournalEntry`, `FactRecorder`, `FactApplier`, `ShipZones`, `GameReplayer`, and `fact/`
  with the `Fact` records
- `projection/` - what a `Viewer` (a player or an observer) may see: `GameProjection.of(view, pendingInput, viewer)`
  (the game, built by `GameProjector`). Plain records that `backend` turns into JSON. It reads through the views and
  never depends on `..operation..`, and nothing else in `core` depends on it (`ArchitectureTest`). See "Projections"
- `state/` - game state model: `Game` (top level), `Turn` (turn number, `GameStep`, current and acting player, the
  Crisis Cards of the turn and whether it returns to the Crisis step, the `DieRoll`, the open interrupt window and its
  cards, read through `GameView.turn()`; `number` counts player turns, not rounds), `Player` (`turns()` counts the player's own turns), `Team` (the winner in
  `GameView.outcome()`), `Die` (d8), `DieRoll` (a roll with the cards played on it), `Randomness`,
  `RemovedComponents`, `PlayerPile`, and the `@Mutates` marker
- `state/interrupt/` - `InterruptWindow` (the windows, as sealed records with the cards each takes) and
  `PlayedInterruptCard`
- `state/view/` - read-only view interfaces (`GameView`, `TurnView`, `DieRollView`, `PlayerView`, `BoardsHolderView`,
  `BattlestarBoardView` (damaged locations), `DecksHolderView` (Skill Cards and civilian ships left), `SkillCheckView`, ...)
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
- `state/util/` - `AllCardsProvider` (builds the full decks in canonical order)
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

### Skill Check Sequence

Each step of `skill-check.drawio` is an event, and each step's processor queues the next one, so a step decides from
the state at that moment what follows.

- A location action or a Crisis Card (`ResolveCrisisCardEventProcessor`, `ChooseCrisisSectionEventProcessor`) calls
  `ops.skillCheck().start(type)` and returns
  `FollowupUtils.beforeSkillCheckFollowups(game)`: a "Political Prowess" decision per player for a location check,
  then `MakeSkillCheckEvent`. That skips the check when "Political Prowess" decided it, and otherwise asks for the
  cards played before the check, from the player to the left of the current player (`eachPlayerFromTheLeft`).
- `RevealCardsBeforeSkillCheckEvent` (and a tiebreak), `ResolveCardsBeforeSkillCheckEvent` (Support the People, the
  Destiny cards, the cards played into the check), `ShuffleAndDivideCardsEvent` (the abilities unless "Restore
  Order" was played, the "consequence" when a card with the ability icon was played in, a Reckless check's Treachery
  cards), `TotalSkillCheckStrengthEvent` (Declare Emergency), `DetermineSkillCheckResultEvent`, then the result, and
  `EndSkillCheckEvent` last.
- The current player resolves the abilities one at a time, in his order (rule 10.4.2).
  `DetermineSkillCheckAbilitiesOrderEvent` offers the ones left as a `OneOf` of `ResolveSkillCheckAbilityEvent`s and
  queues itself after the pick, so an ability whose card was removed first doesn't resolve. Only the cards from the
  shuffle count, and Trust Instincts sends the cards it adds through the same loop. `ResolveSkillCheckAbilityEvent`
  records `AbilityResolved`, once per check. Iron Will, Install Upgrades, All Hands on Deck and Establish Network act
  through that record.
- `EndSkillCheckEvent` runs after everything the result queued: it discards the cards of the check, the ones played
  before it too, and records `SkillCheckEnded`, which clears `GameView.activeSkillCheck()`.
- `DetermineSkillCheckResultEventProcessor.difficulty(game)` is the difficulty so far: Declare Emergency, Jury Rigged,
  and 4 less at the "Airlock" on a character in the "Brig".
- The game board skill checks are the "Admiral's Quarters", the "Brig" and the "Airlock" (rule 10.3.3). The Daybreak
  "Administration" has no skill check: it draws a Mutiny Card, and `ReplaceMutinousPresidentEvent` then lets the
  player choose a new President if the President holds one. `SkillCheckView.targetPlayerNumber()` is the character the
  location check is about, the player himself at the "Brig".

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
- A revealed Cylon ignores the penalties (rule 6.1.2.1). The current player is one when he resolves a Crisis Card at
  "Caprica", so his penalties go through `onCurrentPlayer` and `sendTo`, and "each player" ones through `eachHuman`. A
  result event that targets him checks it too, e.g. `DefendingAPrisonerFailResultEventProcessor` queues no execution.

### Crisis Cards

`ResolveCrisisCardEvent(card)` resolves a Crisis Card. The Crisis step queues it for the card it draws, and so does
`ResolveCapricaCrisisCardEvent` for the one a Cylon chooses at "Caprica". The deck has 136 cards (the Cylon attack
cards are left out, rule A2.7.2), and every one resolves: the Cylon Fleet crises, the base game, Pegasus, Exodus and
Daybreak.

A `CrisisCard` constant lists what is printed on the card: its `SkillCheckType` (`null` for an event crisis), its
`CrisisChooser`, its `CylonActivation` icon and whether it has the "prepare for jump" icon. The images in
`materials/images` are the source. An event crisis always has a chooser, and a skill check crisis has one
(`CURRENT_PLAYER`, `PRESIDENT`, `ADMIRAL` or `CAG`) when it gives an alternate instruction to carry out instead of the
skill check (rule 8.5.1.1). A skill check crisis without a chooser starts its check straight away.

`CrisisCardFollowups` (in `event/crisis`) holds the effects, grouped like `SkillCheckResultFollowups`. A card has two
sections, `TOP` and `BOTTOM`. On a skill check crisis the top is the skill check and the bottom the alternate
instruction.

- `choice(card)` gives a `OneOf` of two `ChooseCrisisSectionEvent`s, one per section. They are `PlayerEvent`s that name
  the chooser, because the engine derives who answers a `OneOf` from its options (see "Engine") and
  most sections name no player. Don't put the effects of a section directly into the `one(...)`. A section a card
  forbids isn't offered: the "nuke token" sections of "Abandon Galactica" and "Build Cylon Detector" need a nuke token.
- `ChooseCrisisSectionEventProcessor` starts the skill check on the top of a skill check crisis, and otherwise asks
  `followup(card, section)` for the effects of the section.
- An effect on the current player goes through `onCurrentPlayer`, because a revealed Cylon resolving the card at
  "Caprica" ignores it (rule 6.1.2.1). So does "each player", for the humans (`eachHuman`).
- "Roll a die. On a 4 or lower, ..." queues `rollDie(new ResolveCrisisRollEvent(card, section))`, and `rollResult` holds
  the number and the effect, so the card text stays in one place.
- Where a title holder picks a player (`ChooseCrisisTargetEvent`: a miracle token, a Mutiny Card, Interrogation, "The
  Circle", "Requested Resignation"), `ChooseCrisisTargetEventProcessor.legalTargets` lists the answers, `null` being the
  card's other instruction. Nobody is asked when there is no legal answer. The answer names its card, which has to be
  the one being resolved: the last one played into the turn.
- "Keep this card in play" (`KeepCrisisCardInPlayEvent`) records the skill check in `GameView.crisisCardsInPlay()`, and
  `EndTurnEvent` leaves that card out of the discard pile. "Detector Sabotage" stops the looks at Loyalty Cards in
  `LookAtLoyaltyCardEventProcessor`, and "Legendary Discovery" counts as 1 distance (see "Jump and Kobol").
- "Fulfiller of Prophecy" returns to the Crisis step after the Activate Cylon Ships step (see "Turn Loop").
- The Activate Cylon Ships step activates the icon of the card drawn last. The Cylon ships go through the same events
  as the skill check crises, e.g. `ActivateRaidersEvent`, in `CrisisCardFollowups.activation`.

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
`DRAW_EXACTLY_2_OF_ANY_COLOR`, `DISCARD_EXACTLY_4`, `DISCARD_DOWN_TO_HAND_LIMIT`, `MUST_LAUNCH_IN_VIPER`,
`DESTINATION_GALACTICA`), and `drawExactly(n)` and `discardExactly(n)` give the counted ones. A processor attaches them to a
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
- The chooser of a `OneOf` is the player of the `PlayerEvent`s in its options, otherwise the current player. That
  works because `PlayerEvent.playerNumber()` is always the acting player: an event that names a target, such as
  `SendToBrigEvent`, is a plain `Event`. Options that name different players are a bug.
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
- Seeds: `GameEngine.start(playerCount, seed)` and `restore(journal, seed)` make a seeded engine, which reseeds its
  `Randomness` (`reseed`) before each input from the seed mixed with the journal's size. So the same seed and inputs
  give the same game, also after a restore. A game started with a `Randomness`, or restored without a seed, gets fresh
  randomness after a restore. `pendingInput()` is the pending input in full, a secret one included.
- Sandbox: `inject(SandboxEvent)` runs a setup event at once, ahead of the pending input, through the same run loop
  (win checks included). `JournalEntry.EventInjected(event)` before its scope tells `AgendaReplayer` to push it, with
  the root as its parent. `submit` never takes a `SandboxEvent`. `undo()` goes back to the pause before the last input:
  the engine knows where each input began (live from `advance`, after a restore from
  `AgendaReplayer.replayWithInputs`: the scope that answered a decision, each `ChoiceMade`, each `EventInjected`), and
  rebuilds from that prefix. A choice that led to a decision is undone on its own; before the first pause there is
  nothing to undo.
- `event/sandbox/` holds the `SandboxEvent` marker and the setup events, plain `Event`s with typed fields (one per kind
  of card): put a Crisis, Destination, Skill or Destiny card on top, give a Skill Card, move a character, place a ship,
  change a resource, set the jump preparation track, damage a location, assign a title. Their processors use the
  rules' ops where one fits, and `SandboxOps` (`GameOps.sandbox()`) for the rest. `DeckFact.CardsPulled` takes a
  stacked or given card out of its draw pile, so no copy appears.

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
  and characters in the "Brig" no Crisis step. Movement and Action are a `OneOf` with a `NoOpEvent`. A pilot gets an
  `ActivateViperEvent` decision instead of a location action, and a character whose location has no action he can take (e.g. in
  "Sickbay", see `LocationEvent.canAct`) gets no Action choice.
- `DiscardStepEvent` asks every player over the hand limit, clockwise from the current player, with
  `DISCARD_DOWN_TO_HAND_LIMIT`: Skill Cards over `handLimit()`, and the President's Quorum Cards.
- The Crisis step draws a card, keeps it with the turn and queues `ResolveCrisisCardEvent` (see "Crisis Cards"). The
  Crisis Cards of a turn stay with it, in `TurnView.crisisCards()` (`GameOps.playCrisisCard`), for the icons the later
  steps read. `EndTurnEvent` discards them (rule 8.8), except the ones kept in play.
- The Activate Cylon Ships step activates the icon of the card drawn last (rule 8.6). "Fulfiller of Prophecy" sets
  `TurnView.returnsToCrisisStep()`: the step then also queues `CrisisStepEvent(again = true)`, which draws and resolves a
  new card and queues the Activate Cylon Ships step of that card. A Crisis step that is skipped queues none.
- `PrepareForJumpStepEvent` happens only when a Crisis Card of the turn has the "prepare for jump" icon, because with
  the Kobol objective the icon is all the step does. It queues an `IncreaseJumpPreparationEvent` per icon, and the
  Mutineer draws a Mutiny Card (rule 8.7.1). `IncreaseJumpPreparationEvent` queues `JumpingTheFleetEvent` when the
  token moves past `MINUS_1`: the track has no "Auto Jump" space, the token goes back to `START`.
- `EndTurnEvent` is the only resource check (rule 16.1). It queues `CylonsWinEvent` at 0, otherwise it passes the
  current player token to the left and queues the next `StartTurnEvent`.
- `GameOps.endTurnEarly()` (`TurnEndedEarly`) ends a turn before its steps do, e.g. when the current player is
  executed. The Movement, Action, Crisis, Activate Cylon Ships and Prepare for Jump steps that are left then do nothing,
  the Discard step and `EndTurnEvent` still happen. `TurnStarted` clears `TurnView.endedEarly()`.
- A Stranded Karl "Helo" Agathon is off the board until `StartTurnEvent` puts him on the Hangar Deck at his player's
  2nd turn (`PlayerView.turns()`, which an execution starts again for the new character). Until then the Movement and
  Action steps skip him, and `SendToBrigEvent` and `SendToSickbayEvent` leave him alone.

### End of Game and Mutiny

- `WinChecks` (`event/endgame/`) holds the Cylon wins that happen at once (rule 16.1): a centurion at the end of the
  Boarding Party track, and 6 damaged Galactica locations. The engine asks after every event, and `ScopeClosed` records
  the result with the event's followup, so restore never runs the checks. The resource check stays in `EndTurnEvent`.
- `CylonsWinEvent` ends the game for the Cylons through `EndGame.end` (the `GameEnded` fact), and `GameView.outcome()` gives the winning `Team`,
  which `Finished(winner)` reports, and `GameView.winners()` the players who won. A test script that plays to the end has to end the game, e.g. with a
  `CylonsWinEvent`.
- To draw a Mutiny Card, queue a `DrawMutinyCardEvent`. Its processor sends the player to the "Brig" on the second card
  (the Mutineer on the third, `PlayerView.isMutineer()`), and asks for a `Discard1MutinyCardEvent` per card over the
  limit (rules 5.2.3.2 and 5.2.3.3). In the "Brig" and off the board (a Stranded Helo) the player only discards. Only
  the Press Room calls `PlayerOps.drawMutinyCard()`, because its target doesn't move.

### Execution

`ExecuteEvent(playerNumber)` follows rule 7.1. Its player carries out the steps, so he is the player of the event.
`ExecuteEventProcessor.canBeExecuted` allows humans only: a revealed Cylon ignores it (rule 6.1.2.1).

- The player discards his Skill and Mutiny Cards and his miracle token.
- A human: lose 1 morale, the character leaves the game (`CharacterRemoved`, into `GameView.removedComponents()`), the
  Loyalty Cards are discarded but the Mutineer card stays, and the player chooses a new character. Once the game is
  under way, `SelectCharacterEventProcessor.canBeSelected` allows any type but a Cylon Leader, and never an executed
  character or its other version. `FinishExecutionEvent` then adds a "You Are Not a Cylon" card to the Loyalty deck and
  deals one (`DealLoyaltyCardsEvent`, with extra cards for Boomer and Baltar before the Sleeper Agent phase), and hands on
  the titles the executed character held. No character left to choose: `CylonsWinEvent`.
- A hidden Cylon reveals a "You Are a Cylon" card (`LoyaltyCardRevealed`), gives his other Loyalty Cards to a human
  (`GiveLoyaltyCardsEvent`) and the Mutineer card too (`PassMutineerCardEvent`), moves to the "Resurrection Ship" and
  loses his titles. From 7 distance on he keeps his other Loyalty Cards. An Infiltrating Cylon Leader only goes back to the "Resurrection Ship" (`InfiltrationEnded`).
- The executed current player's turn ends early (see "Turn Loop").

### Jump and Kobol

The jump is a chain of events, one per step of rule 12.1.1. `JumpingTheFleetEvent` comes from the "FTL Control" action
and from `IncreaseJumpPreparationEvent` (the track has no "Auto Jump" space, the token goes back to `START`).

- `JumpingTheFleetEventProcessor` queues a decision per assault raptor in space (`one(ReturnAssaultRaptorEvent,
  NoOpEvent)`: its pilot, or the current player when unmanned, may keep it there), `ReturnVipersToReservesEvent(true)`,
  `MoveCylonShipsToFleetBoardEvent` and `DrawDestinationCardsEvent`. After 8 distance it queues `EndGameEvent` instead.
- The Admiral's `ChooseDestinationEvent` names both cards, the other goes to the bottom. `ResolveDestinationCardEvent`
  records `DestinationReached` (`GameView.destinations()` and `distanceTraveled()`), queues the card's effects
  (`DestinationCardFollowups`, the card text in its comments), `ObjectiveInstructionsEvent(distanceBefore)` and
  `ResetJumpPreparationEvent`. "Lion's Head Nebula" advances the track after the reset, "Misjump" draws its new card at
  once. `DestinationCard` has the printed distance, and "Mining Asteroid" is left out because the Crisis deck has no
  "Scar". "Legendary Discovery" counts as 1 distance too, through `crisisCardsInPlay()`.
- The cards that ask the Admiral: `RiskShipsEvent` (with `RiskResultEvent` after the roll, only when enough ships are in
  the "Reserves"), `GasGiantEvent`, the "Civilian Convoy" trade and `GasCloudPlaceEvent`. "Ragnar Anchorage" repairs as
  many ships as possible without asking.
- `ObjectiveInstructionsEvent` queues `SleeperAgentPhaseEvent` when the distance reaches 4 for the first time.
  It deals a card per player with `DealLoyaltyCardsEvent` (two to Boomer and Baltar unless revealed Cylons, Motive Cards to
  a Cylon Leader), then `FinishSleeperAgentPhaseEvent` has the revealed Cylons pass on the Mutineer card and hand their
  cards to a human, and the current player choose who draws an extra card when nobody got the Mutineer card. From 7
  distance on no Loyalty Cards are handed off (`FinishSleeperAgentPhaseEventProcessor.canHandOffLoyaltyCards`).
- `RevealMutineerEvent(playerNumber, drawsLoyaltyCard)`: the card stays among the Loyalty Cards and everybody knows it
  (describers show it faceup). The player loses the titles, draws a Mutiny Card and, when dealt it facedown, another
  Loyalty Card.
- `EndGameEvent` follows rules 16.1 and 16.2: each unrevealed Personal Goal of a human reduces its resource by 1
  (`LoyaltyCard.personalGoalResource`), and the Cylons win if a resource is 0. Every ending goes through
  `EndGame.end(game, ops, team)`: the Cylon Leaders reveal the Motive Cards they can (`MotiveCards`, the requirements as
  printed, `MotiveCard.allegiance`), and `GameEnded` carries the winning players (`GameView.winners()`), the Leaders with
  at least two revealed cards of the winning allegiance and at most one unrevealed included (rule 16.3.2).
- Not built yet: revealing a Motive Card or a Personal Goal in the middle of a game, and the Final Five cards.

### Interrupt Cards

Six Skill Cards are played in response to something, outside the Action step: "Strategic Planning", "Calculations",
"Evasive Maneuvers", "Run Interference", "Best of the Best" and "Major Victory". They are played in interrupt windows,
in `event/interrupt/`.

- `InterruptWindow` (`state/interrupt/`) is a sealed set of records, one per window, and `cards()` lists the card types
  each takes: before a die roll, before a piloted viper's attack roll, after a die roll, after a viper is attacked,
  before raiders activate, after a basestar or centurion is destroyed.
- `OpenInterruptWindowEvent(window)` is an ordinary queued event. Its processor asks, as the hands are at that moment,
  each player who can play a card for the window, one at a time and clockwise from the current player
  (`PlayInterruptCardEventProcessor.canPlay`: humans only, rule 6.1.2.3, plus each card's own condition). The question
  is a decision on `PlayInterruptCardEvent`, and no card means a pass. Without holders the window doesn't open and the
  game doesn't pause.
- `PlayInterruptCardEvent` is a `SecretEvent`, so `PendingInput.secret()` is true while it is pending, and
  `GameEngine.status` shows it only to the player who answers. The others get a plain `Waiting`.
- `CloseInterruptWindowEvent` runs once everyone has answered. Two cards of a type conflict ("Run Interference" only in
  the same space area), and the current player keeps one with a `TiebreakInterruptCardsEvent` (rule 13.3), the other
  goes back to its owner's hand. Then each card resolves in play order (`InterruptCardResolved`, into the discard pile)
  and the window closes. Windows never nest: a window closes before what its cards queue runs.
- Die rolls: `FollowupUtils.rollDie(result)` returns `all(window before, RollDieEvent, window after, result)`, and the
  result event reads `TurnView.dieRoll()`. The window before a roll starts a new `DieRoll` (`DieRollStarted`), and
  `DieRollCardPlayed(card, modifier)` records each card that takes effect on it. A reroll is another `DieRolled` and
  keeps the cards (rule A2.1.1.10). The result is clamped to 1-8 after all of them (rule 13.1). An `ArchitectureTest`
  rule lets only `RollDieEventProcessor` call `GameOps.rollDie()`.
- A processor that rolls queues the roll, and a `XxxResultEvent` in its package does what the roll decides, e.g.
  `ArmoryEvent` and `ArmoryResultEvent`.
- Card effects: "Strategic Planning" +2, "Calculations" +1 or -1 once per roll. "Evasive Maneuvers" queues a reroll and
  the same window again, -2 once for a piloted viper. "Best of the Best" marks the attack's own roll, and the attack's
  result destroys that many raiders instead (`DestroyRaidersEventProcessor.destroyRaidersIn`). "Run Interference"
  records `RaidersWillMiss(area, count)`, at most the raiders there, and `ActivateRaiderEventProcessor` counts them
  down with `RaiderMissed`. "Major Victory" is for the acting player, once a turn (`TurnView.resolvedInterruptCards()`),
  and rolls for 1 morale. `TurnStarted` clears the roll, the resolved cards and the misses.

### Combat

The attacks follow the Attack Table (rule 11.6), each split into the attack, which queues the roll with its windows,
and its result.

- `AttackViperEvent(raiderId, humanFighterId)`: a raider attacks a viper or an assault raptor. The window after the
  roll takes "Evasive Maneuvers".
- `AttackGalacticaEvent(cylonShipId)`: a raider or a basestar. A hit is a `one(DamageGalacticaEvent,
  DamagePegasusEvent)`. `ActivateCylonBasestarEvent` queues it unless the weapons are disabled.
- `AttackCylonShipEvent(humanFighterId, cylonShipId)`: a fighter, or Galactica with "Weapons Control" (`byGalactica`),
  attacks a raider, a heavy raider or a basestar. A piloted fighter's roll gets the "Best of the Best" window. A hit on
  a basestar is a `DamageBasestarEvent`, which destroys it at 3 damage (a critical hit counts 2) and opens the "Major
  Victory" window. "Structural Damage" adds 2.
- `ActivateViperEvent(playerNumber, ViperActivation)`: one option of rule 11.1.1. A pilot activates his own viper,
  anyone else an unmanned one. `ProtectTheFleetEvent` takes the same `ViperActivation`, for an unmanned viper, and both
  use `ActivateViperEventProcessor.isValidActivation` and `activate`. The Action step and the "Hangar Deck" offer the
  activation as a decision, and "Command" only when `canActivateUnmanned`.
- Every effect that places or launches a Cylon ship on the main game board goes through `AbstractOps.takeFromSupply`
  (rule 11.4.2): a ship from the supply, and when it has none left, one from the lowest-numbered Cylon space area of the
  Cylon Fleet board. Without one there either, nothing is placed.

### Journal and Replay

The journal records exactly what the game ops did, so a game can be rebuilt from the journal alone. It is fact-sourced: replay applies recorded facts, and never re-runs processors or needs randomness.

- `Fact` (`engine/journal/fact/`) is a sealed interface, grouped by area: `BoardFact`, `DeckFact`, `GameFact`,
  `InterruptFact`, `PlayerFact`, `ShipFact`, `SkillCheckFact`, `TitleFact`. The records are nested in the area interfaces, because a
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
   analyzed. Mutable collections that getters expose (e.g. `CylonShips.raiders()`) can't be marked, don't change
   state through them. Two more rules keep the layers apart: `..state..` depends on none of `..operation..`,
   `..event..` and `..engine..`, and `..event..` doesn't depend on `..engine..`.
6. **State getters return copies.** Lists are `List.copyOf`, so a list a caller holds stays as it was when a later fact
   changes the state, e.g. a loop that discards the cards it iterates. The boards return every collection as a sorted
   copy: `EnumSet`/`EnumMap` for locations and characters, `TreeSet`/`TreeMap` by id (`Ship.BY_ID`) for ships and
   centurions, because `Set.copyOf` and `Map.copyOf` keep no order, and the ops take the first ship of a kind from the
   reserves, the one with the lowest id. The Galactica view hands out no mutable object (`pilotedFighter` gives a
   `HumanFighterView`). The board interfaces have no default writers: each board implements `place` and `remove` (one
   character or one ship) and `damage` (one location) itself, so each change has exactly one public method.
7. Ships and centurions are named by their fixed ids. Decks are built in canonical order and shuffled by
   `GameOps.startGame()`.

### Projections

`projection/` holds what each `Viewer` (`PlayerViewer(n)` or `Observer`) may know. Projections are data, not text:
the frontend owns the wording.

- `GameProjection.of(view, pendingInput, viewer)` is the game. What lies open on the table is public. A hidden card is a
  count, with the colors of Skill Cards (their backs show them), and a field holding cards is `null` for a viewer who
  may not see them (`SkillHand`, `HiddenCards<T>`). `GameProjector`'s javadoc lists the rules: hands, Loyalty and other
  player cards, the President's Quorum hand, looked-at Loyalty Cards and civilian ship cargo, face down discard piles
  (`Disclosure.FACEDOWN_DISCARDS`), cards played before a skill check, an open interrupt window's cards (the others
  count them in the hand), the pending input (backend decision 6), and everything once the game has ended. The Mutineer
  card is faceup once held ("immediately reveal this card").
- `LiveUpdateProjection.of(journal, from, viewer)` is what happened: the scopes (`ScopeLog`: actor, event type, its
  fields when public, the facts) and the choices (`ChoiceLog`). `LiveUpdateProjector` has an exhaustive switch over every
  fact, so a new fact doesn't compile until it has a rule. A fact the viewer may not see becomes `Hidden` (count,
  colors). A shuffle never shows its order, a draw only a count. The scope of a `SecretEvent` shows only to its actor.
  Followups never show. It reads the whole journal, e.g. for who was President when a Quorum Card was drawn.
- What a player learns is a fact (backend decision 8): a player who looks at cards he doesn't take records
  `PlayerFact.CardsSeen` (`PlayerOps.see`), which applies nothing, like `LoyaltyCardSeen` and `CivilianShipRevealed`.
- `Disclosure.CARD_COMPONENTS` marks every event component that holds a card public or private, and a test fails until
  a new one is marked. An event shows its fields only when all its cards are public.
- A civilian ship's id says nothing about its cargo: every shuffle of the civilian ship deck deals the ids anew
  (`AbstractOps.shuffle`), and a ship that goes back into the deck is forgotten by whoever looked at it
  (`CivilianShipForgotten`).

### Backend

`backend` (Spring Boot 4, Java 21, package `xyz.zlatanov.frakkintoasters.backend`) runs one `GameEngine` per game,
stores its journal in Postgres and pushes to every viewer what he may see (`backend-plan.md`). It adds no game rules.

- `journal/` - `JournalCodec` (a `JournalEntry` to JSON and back, `FORMAT_VERSION`), `ValueCodec` (records by their
  components; a value of an interface type carries its `kind`, from the closed `TypeRegistry`; a `List<?>` of cards
  takes its type from the `DeckId` or `PlayerPile` beside it, `cardType()`), `WireCodec` (the projections), and
  `TypeWalker` (every type and its components, for the format lock). `FormatLockTest` checks the format against
  `src/test/resources/journal-format.txt`: a change needs a new `FORMAT_VERSION`, then run it with
  `-DupdateFormatLock=true`. An event is named by its simple name, a fact with its area (`DeckFact.CardsDrawn`).
- `game/` - `GameSessions` runs the commands of a game one at a time (a lock per game): session from the Caffeine
  cache or restored from the rows, the state token (`StateTokens`, the sequence hashed with `frakkin.token-secret`),
  the engine, the new entries and the `game_bundle` summary in one transaction (a failed write evicts the session),
  then the projections to the `ProjectionSink`, under the lock. A viewer for whom nothing visible changed gets nothing.
  `JournalStore`, `GameBundles` (with `mode` and `seed`: a seeded game restores with its seed).
- `user/` - logins through Google and Discord (`LoginProviders`, only those with a client id), the `dev` profile's
  login by name (`POST /api/dev/login`, refused together with a real provider), `AppUserAuthentication` in the HTTP
  session, which Spring Session keeps in Postgres so a login survives a restart, `GET`/`PUT /api/me`. CSRF through the
  `XSRF-TOKEN` cookie and the `X-XSRF-TOKEN` header.
- `lobby/` - `/api/games`: host, list (open lobbies, mine, running ones to watch), join as a player (next free seat, 7
  at most) or an observer, leave, and start by the host with 3 to 7 players (the seats close up to 1..n).
- `live/` - `/ws/games/{id}`: the handshake needs a participant and an allowed `Origin`, and gives the connection its
  `Viewer`. `ConnectionPool` is the `ProjectionSink`; each `LiveConnection` sends from its own queue. Client messages:
  `status`, `history`, `submit` (token, event), `choose` (token, option index). Server messages: `game` (token,
  projection), `update`, `log`, `rejected`. A new connection gets `game`, then `log`.
- `sandbox/` - `POST /api/sandbox {playerCount, seed?}` starts a sandbox (`game_bundle.mode = SANDBOX`, a seeded
  engine, the caller its only participant, with the `Sandbox` viewer, who answers for whoever is asked). The socket
  takes `undo` (deletes the rows after the new sequence; every connection gets `game` and `log` again) and `inject` (a
  `SandboxEvent`) only in a sandbox. `GET /api/games/{id}/journal` exports a sandbox (`formatVersion`, `seed`,
  `playerCount`, `entries`) and `POST /api/sandbox/import` stores one as a new sandbox once the engine restores it.
- Regression journals: `src/test/resources/journals/*.json` are sandbox exports, and `RegressionJournalsTest` checks
  that each restores. A bug found in the sandbox becomes a test: export the journal, drop it there, load it with
  `RegressionJournals.engine(name)` and assert.
- Flyway migrations in `src/main/resources/db/migration`. The tests (`BackendTest`) run against a Testcontainers
  Postgres with the `dev` profile; `LiveGameTest` drives real web socket clients and restarts the server mid-game, and
  `SandboxLiveTest` plays, undoes, exports and imports a sandbox.

### Frontend

`frontend/` is a React 19 + TypeScript (strict) + Vite app, built by Gradle through node-gradle (its own Node 24, `npm
ci`): `./gradlew build` type checks, builds and tests it, and the backend's `bootJar` packs `frontend/dist` under
`static/`, with `AppRoutesController` forwarding the app's routes to `index.html`. React Router, TanStack Query for
REST (the lobby lists poll), Zustand for the game. See `frontend-plan.md`.

- The wire schema: `./gradlew :backend:exportSchema` writes `frontend/src/wire/schema.json` (`backend/wire/WireSchema`:
  every record with its components and type references, unions, enums, the socket messages, the `location` of a
  location's action, and a `role` for each int: `player`, `ship`, `scope` or `count`). `npm run gen` turns it into
  `src/wire/types.ts`. `WireSchemaTest` fails when the checked-in schema is stale, and `tsc` when the UI uses a field
  that went away. `GET /api/schema` serves the same JSON.
- `src/api/` (`http.ts` sends the `XSRF-TOKEN` cookie as `X-XSRF-TOKEN`, `queries.ts` the hooks), `src/live/`
  (`GameSocket` reconnects with backoff, `gameStore` holds the game, its token, the log and the last rejection),
  `src/labels/` (all wording: a humanized fallback, `overrides.ts` for names, templates for events, facts and options;
  only names, never a card's printed text), `src/input/` (the input panel: a choice is a button per option, answered
  by index; a decision is the `EventBuilder`, a form made from the schema, which builds exactly the codec's JSON and
  leaves the player and the constraints to the server), `src/table/` (the state panels, the log), `src/sandbox/`
  (undo, export, restart with the seed, setup by injecting a `SandboxEvent`, the inspector), `src/pages/`.
- The browser holds no game rules: a picker only narrows to plausible answers, the engine judges them.
- The board (`src/table/board/`): `BoardSvg` draws our own schematic (never the publisher's art), laid out in
  `layout.ts`, which has a place for every `Location` (a test checks it). Inputs on the board are in
  `src/input/boardMoves.ts`: a move by clicking a location (asking for a discard when it leaves its ship) and the "Act"
  button on the asked player's location. Each builds the same event, or picks the same option, as the builder; a test
  checks it. The builder stays for everything else.
- The website: `GamesPage` (the lobby: my sandboxes and games, open lobbies, games to watch, hosting), `RoomPage`
  (seats and observers, join, leave, start), `SettingsPage`. A 401 goes to the login page.
- e2e (`frontend/e2e/`): `sandbox.spec.ts` plays a seeded sandbox through a turn, undoes, exports and imports;
  `lobby.spec.ts` has five users host, join, observe, leave and start a game, checks that nobody sees another
  player's cards and that an observer gets no input, and races two users for the last seat.
- Commands: `npm run dev` (Vite on 5173, proxying `/api`, `/ws`, `/oauth2` to 8080; the `dev` profile allows its
  origin), `npm test` (Vitest), `npm run gen`, `npm run e2e` (Playwright, against the running app with the `dev`
  profile, `E2E_BASE_URL` to point elsewhere).

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
- `FakeDie` - test double for `Die` with `nextRoll(int)` to control randomness. A result event test calls
  `rolled(int)` first, which starts and rolls a `DieRoll` the way the window and `RollDieEvent` do
- `FakeDeck<T>` - test double for `Deck<T>` with `nextCard(T...)` to control draws, and `clear()` to empty the draw
  pile
- `core` has test fixtures (`src/testFixtures`, plugin `java-test-fixtures`) for what `backend` tests use too: the
  test-only events with their processors and `TestOps`, `GameSnapshot`, `ScriptedScenario`, `ScriptedTurns` and
  `TestEngines` (starts an engine with any first event). `ArchitectureTest` leaves them out, like the tests
- `GameSnapshot` (`engine/journal/`) - turns a `Game` into comparable plain values. Replay tests compare a live game
  with its replayed twin. `ScriptedScenario.play(journal)` records every kind of fact, which `GameReplayerTest`
  checks. One of its scopes has no event, so only `GameReplayer` rebuilds it, not `GameEngine.restore`
- `ScriptedTurns` is `TurnLoopTest`'s seeded game as a list of inputs, each computed from the game when it's its turn,
  for tests that drive a game from outside (the codec, the sessions, the web socket)
- Op tests also assert the logged facts (`journal.facts()`), and `FactApplierTest` covers each fact
- `GameEngineTest` plays a script of real events through `submit` and `choose`, started by the test-only `ScriptEvent`
  (it calls `ops.startGame()` and queues the script). The registry finds test processors such as
  `ScriptEventProcessor` too, because it scans the whole classpath. At every pause, `restore` has to give the same
  `GameSnapshot` and statuses. The test-only `FailingEvent` changes the game and then throws, to test rollback. Engine
  tests use a seeded `Randomness`, not `FakeDeck`, because restore rebuilds the real decks
- `TurnLoopTest` plays a seeded game through several full turns, restoring at every pause. Its inputs are relative to
  the first player the seed picks. The test-only `StackedStartGameEvent` starts the game like `StartGameEvent` and puts
  simple Crisis Cards on top of the deck (nothing to ask but the section, no die rolls), so the turns don't depend on
  the cards a seed draws
- `LocationSkillCheckTest` runs the "Admiral's Quarters", "Brig" and "Airlock" skill checks from the action to
  `EndSkillCheckEvent` through `submit` and `choose`, restoring at every pause. The cards that decide a check are dealt,
  not drawn: `GiveSkillCardEvent` gives each player the cards he plays, and the test-only `StackDestinyDeckEvent` puts
  known cards on top of the Destiny deck, so each check asserts its total strength and doesn't depend on the seed. Its
  processor records the fact through `TestOps` (in the fixtures' `operation` package), because only the ops can record
  one. `StackCrisisCardEvent` and `SetUpGalacticaEvent` set up the turn that the Airlock check is part of
- `CrisisStepTest` plays the Crisis step to the end of the Activate Cylon Ships step: the skill check or the alternate
  instruction of a card, and "Fulfiller of Prophecy" returning to the step. `CrisisCardResolutionTest` runs every section
  of every card through the real processors with a minimal engine, so a section can't end in an event without a processor
- `InterruptWindowTest` plays interrupt windows through `submit`, restoring at every pause: the secret status, a pass,
  a tiebreak, an "Evasive Maneuvers" reroll. The test-only `GiveSkillCardEvent` and `PlaceRaiderEvent` set up the
  hands and the board, which a seeded game can't choose
- `JumpAndKobolTest` plays four jumps and the Kobol end game through `submit` and `choose`, restoring at every pause: the
  Sleeper Agent phase, a human win, and a Personal Goal that turns it into a Cylon win. The test-only `StackDeckEvent`
  stacks the Destination and Loyalty decks, which a seeded game can't choose
- Tests follow the pattern: set up game state, execute the event, assert state changes and followups. Use real game ops
  in processor tests, not mocks, since processors may read their own changes
