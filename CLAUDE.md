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
- `refactor-game-view.md` - the target design for `GameView` + game ops + journal + describers + engine, including the
  rules game ops must follow (one call is one fact, always log, no decisions inside ops, etc.).

## Roadmap

Next steps, in order:

1. Complete the skill check sequence according to `skill-check.drawio`.
2. Reanalyze existing events to decide which need to be a `PlayerEvent` (carry `playerNumber`) and where the current
   player is enough. See the todos in `PlayerEvent` and `ActionEvent`.
3. Create the journal and tie it to the ops classes in `operation/` (`AbstractOps` has the todo). Follow the design in
   `refactor-game-view.md`.
4. Create the game engine to drive the events and turns (`Turn` is a stub; there is no event-to-processor lookup in
   production code yet).
5. Create a working frontend with the events that exist by then.

## Project Structure

Multi-module Gradle project with a single `core` module. Package root: `xyz.zlatanov.frakkintoasters`.

### Key Packages

- root - `EventProcessor` (base class for all processors), `Turn` (empty placeholder for the future engine)
- `event/` - event records and their processors, grouped by area (`action`, `board`, `crisis`, `deck`, `endgame`,
  `location/{galactica,pegasus,colonialone,cylon}`, `loyalty`, `officials`, `player`, `ship`, `skillcheck`,
  `skillcheck/ability`, `skillcheck/result`). Also `placeholder/` (events that need player input) and `constraint/`
  (`EventConstraint`)
- `operation/` - game ops, the only code that should mutate state: `GameOps` (entry point) hands out `PlayerOps`,
  `ShipOps`, `GalacticaOps`, `PegasusOps`, `CylonFleetOps`, `SkillCheckOps`, `LoyaltyDeckOps`, `TitleOps`. They extend
  `AbstractOps`, which holds the `Game` (journal still a todo)
- `state/` - game state model: `Game` (top level), `Player`, `Die` (d8), `GameStep`, `RemovedComponents`
- `state/view/` - read-only view interfaces (`GameView`, `PlayerView`, `BoardsHolderView`, `SkillCheckView`, ...)
  implemented by the state classes. Processors read state only through these
- `state/board/` - board hierarchy: `Board` (base, tracks characters by location) -> `BattlestarBoard` (adds damage
  tracking) -> `GalacticaBoard`/`PegasusBoard`. Also `CylonFleetBoard`, `SpaceLocationsBoard`, `BoardsHolder` (aggregates
  all boards). The `Location` enum defines all 34 board spaces in scope
- `state/ship/` - `Ship` interface with implementations: `Viper`, `ViperMarkVII`, `Raptor`, `AssaultRaptor`,
  `Basestar`, `Raider`, `HeavyRaider`, `Centurion`, `CivilianShip`. `CylonShips` manages the Cylon ship supply. Ships
  have unique integer IDs
- `state/deck/` - generic `Deck<T>` (draw/discard/shuffle). `DecksHolder` manages all game decks and routes discards to
  the correct deck
- `state/skill/` - `SkillCard` (record), `SkillCardType`, `SkillCardColor` (6 colors), `SkillCheck` (active check with
  its card piles), `SkillCheckType`, `PlayedCard`
- `state/card/` - card enums: `LoyaltyCard`, `QuorumCard`, `MutinyCard`, `ObjectiveCard`, `DestinationCard`,
  `MotiveCard`
- `state/crisis/` - `CrisisCard`, `SuperCrisisCard`
- `state/damage/` - damage card enums: `GalacticaDamage`, `PegasusDamage`, `BasestarDamage`
- `state/track/` - `BoardingParty`, `JumpPreparation`, `Pursuit`
- `state/character/` - `Character` enum (33 BSG characters), `CharacterType` enum
- `state/exception/` - `InvalidActionException` (an event failed validation) and `FrakCallTheAdmiralException` (should
  never happen, i.e. a programming error)

## Architecture

### Events and Processors

Events are plain records with no logic. `Event` is an empty marker interface. `PlayerEvent` adds `playerNumber()`.
`LocationEvent` adds `location()`. `ActionEvent extends PlayerEvent` is under review (see the todos in `PlayerEvent`
and `ActionEvent`).

Each `XxxEvent` has an `XxxEventProcessor extends EventProcessor<XxxEvent>` in the same package. Keep that naming:
`EventTestHarness` finds the processor by it. `EventProcessor.execute(Game, event)`:

1. sets the context: `game` (a `GameView`), `event`, and helper views `galacticaBoard`, `pegasusBoard`,
   `cylonFleetBoard`, plus `player` when the event is a `PlayerEvent`
2. calls `init()` (optional setup, marked for removal)
3. calls `isValid()` and throws `InvalidActionException` if it returns false
4. creates `ops` (`GameOps`) and calls `process()`, which mutates state through `ops` and returns a `Followup`

`ops` is only set right before `process()`, so `init()` and `isValid()` can't change state. Processors read through
`GameView` and write only through game ops. Game ops do mechanics, and processors make decisions (targets, rules
branching, followups).

Placeholder events (`event/placeholder/`: `PlayerDecisionEvent`, `PlayQuorumCardEvent`, `PlaySuperCrisisCardEvent`)
have no processor. They stand for a point where a player has to choose. `PlayerDecisionEvent<T>(playerNumber,
Class<T> action, List<EventConstraint>)` means "this player picks a concrete `T` event".

`FollowupUtils` builds shared followup sequences, e.g. `beforeSkillCheckFollowups(game)`.

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
                new ResolveCapricaCrisisEvent(playerNumber)));
```

### Event Constraints

`EventConstraint` is a plain enum of extra rules that can be pinned onto an event (e.g. `DRAW_EXACTLY_2`,
`DISCARD_EXACTLY_4`, `MUST_LAUNCH_IN_VIPER`, `DESTINATION_GALACTICA`). A processor attaches them to a
`PlayerDecisionEvent`, e.g.
`new PlayerDecisionEvent<>(n, DrawSkillCardsEvent.class, DRAW_EXACTLY_2)`, and they are forwarded to the concrete event
when the decision is resolved.

An event that supports a constraint has its own record component for it (e.g. `DrawSkillCardsEvent.drawLimit`, `null`
when there is none). Its processor checks the constraint in `isValid()` and throws `FrakCallTheAdmiralException` for any
constraint it doesn't support.

### Lombok Usage

Uses Lombok extensively: `@Builder`, `@Getter`, `@Accessors(fluent = true)`, `@RequiredArgsConstructor`, `val`.
Accessors are fluent style (no `get`/`set` prefix), e.g. `game.players()` not `game.getPlayers()`. Prefer Lombok `val`
when possible.

### Testing Patterns

- JUnit 5 + Mockito
- `TestHarness` - base class that builds a `Game` with fake components and exposes the boards, decks, ships and an
  `ops` instance. Game op tests (`operation/`, e.g. `PlayerOpsDrawSkillCardTest`) extend it directly
- `EventTestHarness<E>` - base class for processor tests. Name the test `XxxEventProcessorTest` in the processor's
  package and the harness creates the processor by reflection. Use `execute(event)`, `assertFollowup(...)`,
  `assertInvalid(event)`, `assertSkillCheckTriggered(type)`. If a test doesn't assert a followup, the harness checks
  that it was `Followup.NONE`
- `FakeDie` - test double for `Die` with `nextRoll(int)` to control randomness
- `FakeDeck<T>` - test double for `Deck<T>` with `nextCard(T...)` to control draws
- Tests follow the pattern: set up game state, execute the event, assert state changes and followups. Use real game ops
  in processor tests, not mocks, since processors may read their own changes
