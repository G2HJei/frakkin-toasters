# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Web version of the Battlestar Galactica board game. Java implementation modeling the game's state, events, and
mechanics.

## Build Commands

```bash
./gradlew build          # Build the project
./gradlew test           # Run all tests
./gradlew :core:test     # Run tests in the core module
./gradlew :core:test --tests "xyz.zlatanov.frakkintoasters.event.player.MoveEventProcessorTest"  # Run a single test class
```

## Project Structure

Multi-module Gradle project with a single `core` module. Package root: `xyz.zlatanov.frakkintoasters`.

### Key Packages

- `state/` - Game state model: `Game` (top-level), `Player`, `Die` (d8)
- `state/board/` - Board hierarchy: `Board` (base, tracks characters by location) -> `BattlestarBoard` (adds damage
  tracking) -> `GalacticaBoard`/`PegasusBoard`. Also `CylonFleetBoard` and `BoardsHolder` (aggregates all boards).
  `Location` enum defines all 38 board spaces.
- `state/ship/` - `Ship` interface with implementations: `Viper`, `Raptor`, `Basestar`, `Raider`, etc. `CylonShips`
  manages the Cylon ship supply pool. Ships have unique integer IDs.
- `state/deck/` - Generic `Deck<T>` (draw/discard/shuffle). `DecksHolder` manages all game decks and routes discards to
  the correct deck.
- `state/skill/` - `SkillCard` (record), `SkillCardType` enum, `SkillCardColor` enum (6 colors)
- `state/card/` - Card enums: `LoyaltyCard`, `QuorumCard`, `MutinyCard`, `ObjectiveCard`, `DestinationCard`,
  `MotiveCard`
- `state/damage/` - Damage card enums: `GalacticaDamage`, `PegasusDamage`, `BasestarDamage`
- `state/character/` - `Character` enum (33 BSG characters), `CharacterType` enum
- `event/` - Event-driven game actions (see architecture below)

## Architecture

### Event System

Game actions are modeled as events implementing the `Event` interface:

- `execute(Game)` validates then applies the event, returning a single `Followup`. Followup can contain concrete events
  ready for execution or placeholder events (in the package `xyz.zlatanov.frakkintoasters.event.placeholder`) that need
  further interaction with the players to continue.
- `isValid(Game)` checks preconditions (default: true)
- `apply(Game)` mutates game state and returns a `Followup` (use `Followup.NONE` when there is nothing to follow up
  with; that is also the default)

Event hierarchy: `Event` -> `PlayerEvent` (adds player context) -> `ActionEvent` (location-based board actions)

**Followups** chain events using a recursive sealed interface with four variants:

- `Followup.None` - no further events (singleton `Followup.NONE`)
- `Followup.Single(Event)` - wraps a single event (leaf node)
- `Followup.AllOf(List<Followup>)` - execute all children in order (sequence)
- `Followup.OneOf(List<Followup>)` - current player picks exactly one (choice)

Factory methods: `single(event)`, `all(events...)`, `one(events...)`. Because the structure is recursive, `all(...)`
and `one(...)` accept either plain `Event` varargs (auto-wrapped in `Single`) or nested `Followup` varargs — enabling
patterns like "choose between a single event or a sequence of events":

```java
return one(
        single(new PlaySuperCrisisCardEvent(playerNumber)),

all(new DrawCrisisCardsEvent(playerNumber),
                new

ResolveCapricaCrisisEvent(playerNumber)));
```

### Event Constraints

`EventConstraint` is a plain enum listing optional rules that can be attached to events (e.g. `DRAW_EXACTLY_2`). It lets
`PlayerDecisionEvent` (or any other caller) pin extra rules onto a concrete event without new classes or generics.

- `Event` declares `default List<EventConstraint> eventConstraints() { return List.of(); }`. Events that opt in add a
  `List<EventConstraint>` record component — the record's generated accessor overrides the default.
- `Event.execute(Game)` calls `isValidConstraint(Game, EventConstraint)` on each passed constraint (throwing
  `InvalidActionException` if any returns false); `isValid(Game)` runs afterwards for the remaining preconditions. The
  default `isValidConstraint` throws `FrakCallTheAdmiralException`, so an event that receives a constraint it does not
  handle fails loudly.
- Events that support constraints interpret each one inside `isValidConstraint(Game, EventConstraint)` — one constraint
  at a time, separate from the core `isValid(Game)` preconditions.
- `PlayerDecisionEvent<T>` holds `Class<T> action` plus a `List<EventConstraint>`. When the decision is resolved, the
  constraints are forwarded to the concrete event.
- Example: `ReceiveSkillCardsEvent` has an `eventConstraints` component; passing `DRAW_EXACTLY_2` enforces that the
  selection sums to exactly 2 alongside the existing human/cylon validation.

### Lombok Usage

Uses Lombok extensively: `@Builder`, `@Getter`, `@Accessors(fluent = true)`, `@RequiredArgsConstructor`, `val`.
Accessors are fluent style (no `get`/`set` prefix) - e.g., `game.players()` not `game.getPlayers()`. Lombok `val` is
preferred when possible.

### Testing Patterns

- JUnit 5 + Mockito
- `FakeDie` - test double for `Die` with `nextRoll(int)` to control randomness
- `FakeDeck<T>` - test double for `Deck<T>` with `nextCard(T)` to control draws
- Tests follow the pattern: set up `Game` via builder, execute event, assert state changes and followups
