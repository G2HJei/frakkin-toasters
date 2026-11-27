# CLAUDE.md

Web version of the Battlestar Galactica board game. The `core` module (Java) holds the game's rules and engine, the
Spring Boot `backend` serves games to browsers, and the React `frontend` is the website and the table.

This file holds the global architecture and decisions. The details are in the topic files under `agents/` (see
[Topic files](#topic-files)): read the ones for the code you touch, not all of them. A change that makes a line of a
topic file wrong fixes that line in the same change.

## Scope

The rules and card images are in `materials-sorted/` (`bsg-rules/Battlestar_Galactica_CR2-0.pdf` and its Appendix are the
combined rulebook v2). The first version covers the full game with the Kobol objective.

Out of scope: everything that only applies with the New Caprica, Ionian Nebula or Earth Objective Cards. The rulebook
marks these rules with a blue, green or orange background. Don't model them, and remove them if you find them in the
code. That includes:

- New Caprica: the New Caprica board and its locations (Detention, Resistance HQ, ...), New Caprica crisis cards and
  occupation forces
- Ionian Nebula: trauma tokens, allies, crossroads cards and the alternate basestar damage tokens
- Earth: the Demetrius and Rebel Basestar boards, mission cards, the basestar allegiance marker and the Hub Destroyed
  overlay

## Status

Built and covered by tests: every component of the Kobol game (every Crisis, Super Crisis, Destination, Skill, Quorum,
Mutiny, Loyalty and Motive Card, every location and Title Card action, Pegasus' destruction, the 99 character
abilities), the engine with its journal, restore and rollback, the backend, the sandbox, the table with its inputs, and
the session around a game (seats that change hands, votes, presence, chat, notifications, results and rematch).
`roadmap.md` milestones A to D are done: random games play to an ending (`./gradlew :core:fuzz`; 10,000 seeds per
player count leave only `NO_END` games that are a valid stall: every human in the "Brig", where nobody draws a Crisis
Card), legal-move hints, the table's inputs (`playthrough.spec.ts` plays a game to an ending through the answers), and
sessions that survive real people (`SessionExitTest`: a game across restores with a seat handed over, to its results).
Next: milestones E (replays) and F (hosting; the CI and the deploy to the VPS are done) of `roadmap.md`. `choices.md` records the choices made where the cards
and rules are silent.

## Build Commands

```bash
./gradlew build          # Build the project
./gradlew test           # Run all tests
./gradlew :core:test     # Run tests in the core module
./gradlew :core:test --tests "xyz.zlatanov.frakkintoasters.event.player.MoveEventProcessorTest"  # Run a single test class
./gradlew :backend:test  # Run tests in the backend module (Testcontainers: Docker has to run)
./gradlew :core:fuzz -Pgames=100 -Pplayers=3-7  # Random games played to an ending, writes fuzzer-report.md
./gradlew :core:fuzz -Pplayers=4 -Pseed=117     # One game, with a full trace in core/build/fuzz
docker compose up -d     # Postgres for local runs, on port 5433
./gradlew :backend:bootRun --args='--spring.profiles.active=dev'  # builds the frontend and serves the whole app on 8080
```

Frontend commands are in [agents/frontend.md](agents/frontend.md#commands).

## Design Documents

- `skill-check.drawio` - the full skill check sequence: before-check cards, playing cards in, abilities, result,
  consequences, and the optional followup events.
- `backend.drawio` - the backend: logins, lobby, the live connections, `GameSessions` and the tables.

## Project Structure

Multi-module Gradle project on Java 21: `core` (no libraries but Lombok), `backend` (Spring Boot 4, package
`xyz.zlatanov.frakkintoasters.backend`) and `frontend` (React 19 + TypeScript + Vite, built through node-gradle).
Package root: `xyz.zlatanov.frakkintoasters`. The processor registry scans the whole root package, so nothing in
`backend` may be named `XxxEventProcessor`. The map of `core`'s packages is [agents/packages.md](agents/packages.md).

## Architecture

A player's input goes to the `GameEngine`, which runs events through their processors. A processor reads the game
through the views (`state/view/`), decides, and changes it only through the game ops (`operation/`). The ops record
facts in the journal, and `FactApplier` applies each fact: it is the only code that changes components. A restore
applies the facts again. The projections cut the game per viewer, the backend sends them, the frontend draws them.

- **Events are plain records, without logic.** `Event` is an empty marker; `PlayerEvent` adds `playerNumber()`, which
  is always the acting player (an event that names a target is a plain `Event`). An event that always involves the
  current player is a plain `Event` and reads `GameView.currentPlayer()`. `GameView.actingPlayer()` is the player
  acting right now: the current player unless another one acts during his turn.
- **One processor per event.** Each `XxxEvent` has an `XxxEventProcessor extends EventProcessor<XxxEvent>` in the same
  package; `ProcessorRegistry` and `EventTestHarness` find it by that name. `prepare(view, event)` validates and can't
  change state; `run(ops)` changes it and returns a `Followup`. Ops do mechanics, processors make decisions (targets,
  rules branching, followups).
- **`isValid()` is the only rule check.** A client's event and a queued event go through the same check, and a queued
  event that fails it is a bug (`FrakCallTheAdmiralException`; rejected input throws `InvalidActionException` and
  changes nothing). A rule another processor needs is a public static check on its processor. Only what can happen is
  offered, and a decision always has a legal answer, or the game waits forever.
- **Followups chain the events.** `Followup` is `None`, `Single`, `AllOf` or `OneOf`; the engine's `Agenda` runs them
  depth first and pauses at a `PlayerDecisionEvent` (the player submits a concrete event) or a `OneOf` (he picks an
  option). Each step queues the next, deciding from the state when it runs; an offer that depends on what the effects
  before it did is an event that builds the offer when it runs.
- **The engine holds no game rules**: processors decide. Anything thrown after an accepted input rolls the call back.
- **The journal is fact-sourced.** Every change is a fact that applies exactly what it says, with every random or
  looked-up result in it. Randomness comes only from `Game.randomness()`. Replay never re-runs processors; a seeded
  engine gives the same game for the same inputs, also after a restore.
- **Layers** (`ArchitectureTest`): `@Mutates` component methods are called only from `..journal..` and `..state..`;
  `..state..` depends on none of `..operation..`, `..event..` and `..engine..`; `..event..` doesn't depend on
  `..engine..`; `projection/` reads through the views, never depends on `..operation..`, and nothing else in `core`
  depends on it. State getters return copies.
- **Exhaustive switches**: over `Location`, `SkillCardType`, `QuorumCard`, `MutinyCard`, `CharacterAbility`,
  `SkillCheckType`, the facts (`FactApplier`, `LiveUpdateProjector`), so a new constant doesn't compile until it's
  mapped.
- **One record per played card or ability.** Skill, Quorum and Mutiny Cards and the sheets' "Action:" and "Movement:"
  abilities each have an event per card, a base processor whose final `isValid()` and `process()` call the card's hooks,
  and a static `canPlay`/`canUse` that the offers and the legal answers ask. A test checks one event per card.
- **Card text lives in one place**: the followup builders (`CrisisCardFollowups`, `SkillCheckResultFollowups`, ...)
  hold each card's effects with its text in a comment. Code cites the rule it follows (`rule 6.1.2.1`).
- **Hidden information is the projections' job.** Facts hold everything; `projection/` decides what each viewer sees,
  as data, not text. What a player learns is a fact.
- **Legal answers come from the engine.** `LegalAnswers` adds no rule: the processors' `prepare` judges every
  candidate. The table and the fuzzer answer from them.
- **No rules outside `core`.** The backend runs one `GameEngine` per game, stores its journal in Postgres and pushes to
  every viewer what he may see. The browser only narrows to plausible answers, and all wording is in
  `frontend/src/labels/`.

## Conventions

- Lombok everywhere: `@Builder`, `@Getter`, `@Accessors(fluent = true)`, `@RequiredArgsConstructor`, `val`. Accessors
  are fluent (`game.players()`, not `game.getPlayers()`). Prefer `val` when possible.
- Tests use real game ops, not mocks. Engine tests play through `submit` and `choose` and restore at every pause. See
  [agents/testing.md](agents/testing.md).
- `@Source` names the card or ability an event exists for where its fields don't (see
  [agents/events.md](agents/events.md)).

## Topic files

| File | Read it for |
|---|---|
| [packages.md](agents/packages.md) | where things live in `core` |
| [events.md](agents/events.md) | writing an event or processor: `PlayerEvent`, `@Source`, `FollowupUtils`, followups, constraints, validation |
| [engine.md](agents/engine.md) | `GameEngine` (run loop, who chooses, input checks, rollback, restore, seeds, sandbox, undo, anytime input, only answers) and `LegalAnswers` |
| [journal.md](agents/journal.md) | facts, ops, `FactApplier`, replay, the rules for facts and ops |
| [projections.md](agents/projections.md) | what each viewer may see: `GameProjection`, `LiveUpdateProjection`, `Disclosure` |
| [turn-loop.md](agents/turn-loop.md) | setup, the steps of a turn, `Turn`, ending a turn early |
| [skill-checks.md](agents/skill-checks.md) | the skill check sequence and its results |
| [crisis-cards.md](agents/crisis-cards.md) | Crisis and Super Crisis Cards |
| [actions.md](agents/actions.md) | the Action step, Skill Card "Action:"/"Movement:" plays, grants, repeated viper activations |
| [quorum-and-mutiny-cards.md](agents/quorum-and-mutiny-cards.md) | Quorum Cards and Mutiny Cards |
| [cylons-and-loyalty.md](agents/cylons-and-loyalty.md) | the Cylon reveal, Loyalty Cards, Final Five, Personal Goals, Motive Cards, Cylon Leaders, execution |
| [jump-and-endgame.md](agents/jump-and-endgame.md) | the jump, Destination Cards, the Sleeper Agent phase, the end of the game, titles in the "Brig" |
| [interrupts.md](agents/interrupts.md) | interrupt windows and cards, die rolls |
| [combat.md](agents/combat.md) | attacks, damage, Pegasus' destruction, the nuke, the CAG's actions, placing and activating Cylon ships |
| [character-abilities.md](agents/character-abilities.md) | the 99 character abilities, by where they hook in |
| [backend.md](agents/backend.md) | codec and format lock, `GameSessions`, logins, lobby, live socket, sandbox, regression journals, migrations, CI and deploy |
| [frontend.md](agents/frontend.md) | frontend build, wire schema, labels, answers, pages, e2e |
| [frontend-table.md](agents/frontend-table.md) | the table: layout, log, animations, board, board answers, hover, reference, art |
| [testing.md](agents/testing.md) | harnesses, fixtures, which engine test covers what, the fuzzer |
