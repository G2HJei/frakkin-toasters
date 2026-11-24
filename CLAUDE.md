# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Web version of the Battlestar Galactica board game. Java implementation modeling the game's state, events, and
mechanics. The game-logic `core` module holds the rules and the engine, and the Spring Boot `backend` module serves
games to browsers (see "Backend"), and the React `frontend` module is the website and the table (see "Frontend").

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

## Design Documents

- `skill-check.drawio` - the full skill check sequence: before-check cards, playing cards in, abilities, result,
  consequences, and the optional followup events.
- `backend.drawio` - the backend: logins, lobby, the live connections, `GameSessions` and the tables.

## Roadmap

Next steps, in order:

1. ~~Complete the skill check sequence according to `skill-check.drawio`.~~ Done, see "Skill Check Sequence".
2. ~~Reanalyze existing events: `PlayerEvent` or current player.~~ Done. `GrantEvent` calls `GameOps.actingPlayer`
   for "Executive Order" and "State of Emergency" (see "Actions and Skill Cards").
3. ~~Create the journal and tie it to the ops classes in `operation/`.~~ Done, see "Journal and Replay", with
   per-viewer hiding (`projection/`, see "Projections") and JSON (the backend's `JournalCodec`). The pending followups
   are the engine's `Agenda`.
4. ~~Create the game engine to drive the events and turns.~~ Done: `GameEngine` runs, restores and rolls back a game
   (see "Engine") and plays its turns (see "Turn Loop"), and every component of the Kobol game is built: every Crisis,
   Super Crisis, Destination, Skill, Quorum, Mutiny, Loyalty and Motive Card, every location and Title Card action,
   Pegasus' destruction and the 99 character abilities (`choices.md` records the choices made where the cards and
   rules are silent).
5. ~~Create a working frontend with the events that exist by then.~~ Done:
   the backend, the sandbox, the wire schema, the table with the board, the lobby and rooms (see "Backend" and
   "Frontend"). The inputs on the board came with step 8.
6. ~~Prove the game is completable (`roadmap.md` milestone A): random games played to an ending.~~ Done:
   `./gradlew :core:fuzz` plays the games and writes `fuzzer-report.md`, a finding exports as a regression journal
   with its failing input, and hostile inputs probe the checks. 10,000 seeds per player count ran with no finding left
   but NO_END games that are a valid stall: every human in the "Brig", where nobody draws a Crisis Card.
7. ~~Legal-move hints (`roadmap.md` milestone B).~~ Done: `LegalAnswers` in `core` (see
   "Legal answers"), in the asked viewer's `input` (see "Projections"), read by the table's answers,
   and the fuzzer answers from them (see "Random playouts").
8. ~~The table's inputs (`roadmap.md` milestone C).~~ Done: the answers panel (see "Answers"),
   the board and the mats answering, what the game waits for (`Topic`), the log's story, the animations (see "The
   table") and the reference. `playthrough.spec.ts` plays a game to an ending through the answers. Next: milestone D
   of `roadmap.md`, sessions that survive real people.

## Project Structure

Multi-module Gradle project on Java 21: `core` (no libraries but Lombok), `backend` (Spring Boot 4, package
`xyz.zlatanov.frakkintoasters.backend`) and `frontend` (React, built through node-gradle). Package root: `xyz.zlatanov.frakkintoasters`. The processor registry scans the
whole root package, so nothing in `backend` may be named `XxxEventProcessor`.

### Key Packages

- root - `EventProcessor` (base class for all processors)
- `event/` - event records and their processors, grouped by area (`action`, `board`, `character`, `crisis`, `deck`,
  `endgame`, `interrupt`, `jump`, `location/{galactica,pegasus,colonialone,cylon}`, `loyalty`, `officials`, `player`,
  `sandbox`, `ship`, `skillcheck`, `skillcheck/ability`, `skillcheck/result`, `turn`). The attacks and the viper
  activation are in the root package. Also `placeholder/` (`PlayerDecisionEvent`) and `constraint/`
  (`EventConstraint`, `ConstrainedEvent`). `location/` holds `LocationActionProcessor`, the base of the location action
  processors. `action/` holds `Actions` (what a player can do now), the Skill, Quorum and Mutiny Card plays, the
  character actions and movements, and the grants; below it an event per Skill Card played for its "Action:" or
  "Movement:" (`action/skillcard/`), per Quorum Card (`action/quorum/`), per Mutiny Card (`action/mutiny/`), per
  "Action:" and "Movement:" ability of a character sheet (`action/character/`), and the Title Card actions
  (`action/title/`). `character/` holds `CharacterAbilities`, the bans, costs and penalties, and the events of the
  other character abilities (see "Character Abilities"). `loyalty/` holds the Cylon reveal, the Final Five, the
  Personal Goals and the Motive Cards (see "Cylons and Loyalty Cards")
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
- `engine/answer/` - `LegalAnswers` (the legal answers to a decision, see "Legal answers"), `ShapedAnswer`, `Pick`,
  and the search: `AnswerClasses`, `Domains`, `Shapes`, `AnswerSearch`
- `engine/journal/` - `Journal`, `JournalEntry`, `FactRecorder`, `FactApplier`, `ShipZones`, `GameReplayer`, and `fact/`
  with the `Fact` records
- `projection/` - what a `Viewer` (a player or an observer) may see: `GameProjection.of(view, pendingInput, viewer)`
  (the game, built by `GameProjector`). Plain records that `backend` turns into JSON. It reads through the views and
  never depends on `..operation..`, and nothing else in `core` depends on it (`ArchitectureTest`). See "Projections"
- `state/` - game state model: `Game` (top level), `Turn` (turn number, `GameStep`, current and acting player, the
  Crisis Cards of the turn and whether it returns to the Crisis step, the `DieRoll`, the open interrupt window and its
  cards, the Skill Cards played for their abilities, the "Preventative Policy" loss reductions, the "Negotiation"
  hold, the `TurnLimit`s each player used (what is limited to once a turn, e.g. the CAG's pilot action,
  `TurnView.hasUsed(n, limit)`) and the character abilities each used (`TurnView.hasUsed(n, ability)`), read through
  `GameView.turn()`; `number` counts player turns, not rounds), `Player` (`turns()` counts the player's own turns), `Team` (the winner in
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
  `MotiveCard`, and `QuorumCardInPlay` (a Quorum Card in play with its holder)
- `state/crisis/` - `CrisisCard` (with its printed `CrisisChooser` and `CylonActivation`), `SuperCrisisCard`
- `state/damage/` - damage card enums: `GalacticaDamage`, `PegasusDamage`, `BasestarDamage`
- `state/track/` - `BoardingParty`, `JumpPreparation`, `Pursuit`
- `state/character/` - `Character` enum (33 BSG characters), `CharacterType` enum, `CharacterAbility` (the 99 abilities
  of the sheets, with their character, `AbilityKind` and the miracle tokens a use spends)
- `state/util/` - `AllCardsProvider` (builds the full decks in canonical order; the Skill decks follow the strengths of
  Appendix A2.3.8, `AllCardsProviderTest`)
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

The placeholder event (`event/placeholder/`: `PlayerDecisionEvent`) has no processor. It stands for a point where a
player has to choose. `PlayerDecisionEvent<T>(playerNumber,
Class<T> action, List<EventConstraint>)` means "this player picks a concrete `T` event".

`@Source` (`event/`) names the card or ability an event exists for, where its fields don't: every event of a character
ability (`event/character/`, `event/action/character/`) and every `Offer...Event` has one, which `EventSourcesTest`
checks. On an enum constant it names what the constant stands for, e.g. each `GrantEvent.GrantedBy`. `EventSources.of`
reads it, and the table names it beside the event (see "Frontend"), so a player sees why he is asked.

`FollowupUtils` builds shared followup sequences, e.g. `beforeSkillCheckFollowups(game)`, and holds the helpers that
`SkillCheckResultFollowups` and `CrisisCardFollowups` share, e.g. `lose`, `gain` and `discard`. `playersFrom(game, n)`
lists the players clockwise from player `n`, which is the order of the player numbers.

### Skill Check Sequence

Each step of `skill-check.drawio` is an event, and each step's processor queues the next one, so a step decides from
the state at that moment what follows.

- A location action or a Crisis Card (`ResolveCrisisCardEventProcessor`, `ChooseCrisisSectionEventProcessor`) calls
  `ops.skillCheck().start(type)` and returns
  `FollowupUtils.beforeSkillCheckFollowups(game)`: "Mysterious Intuition" for a Crisis Card's check when Boomer can
  use it, a "Political Prowess" offer per player for a location check, then `MakeSkillCheckEvent`. That skips the
  check when one of them decided it, and otherwise asks for the
  cards played before the check, from the player to the left of the current player (`eachPlayerFromTheLeft`).
- "Political Prowess", "Change of Plans" and "Declare Emergency" are offered to every player in turn
  (`FollowupUtils.playOrPassDecisions`), each through an `OfferPlayOrPassEvent(n, card)` that builds his choice when it
  runs, so a card another player played first counts: the play or the pass while he holds the card and the check allows
  it (`OfferPlayOrPassEventProcessor.canPlay`), otherwise the pass alone. That one option is a `OneOf` built directly, so
  he is still asked and the table doesn't learn who holds the card.
- `RevealCardsBeforeSkillCheckEvent` (and a tiebreak), `ResolveCardsBeforeSkillCheckEvent` (Support the People, the
  Chief of Staff, "Fast Learner", the Destiny cards, the cards played into the check, then "Blind Devotion"),
  `ShuffleAndDivideCardsEvent` ("Quick Fix" and "Human Delusion" once the cards are revealed, the abilities unless
  "Restore Order" was played, the "consequence" when a card with the ability icon was played in, a Reckless check's
  Treachery cards), `TotalSkillCheckStrengthEvent` (Declare Emergency), `DetermineSkillCheckResultEvent`, then the
  result, `OfferCommandAuthorityEvent` (queued only when someone has "Command Authority" and its token), and
  `EndSkillCheckEvent` last. The character abilities in a check are under "Character Abilities".
- A card put into the check goes face down onto its pile of cards, even an empty one ("Guts and Initiative" skips the
  Destiny cards), until `ShuffleAndDivideCardsEvent` records `SkillCheckFact.CardsDivided`
  (`SkillCheckView.cardsDivided()`). A card added after that goes face up onto its pile, e.g. "Force Their Hand".
- The current player resolves the abilities one at a time, in his order (rule 10.4.2).
  `DetermineSkillCheckAbilitiesOrderEvent` offers the ones left as a `OneOf` of `ResolveSkillCheckAbilityEvent`s and
  queues itself after the pick, so an ability whose card was removed first doesn't resolve. Only the cards from the
  shuffle count, and Trust Instincts sends the cards it adds through the same loop. `ResolveSkillCheckAbilityEvent`
  records `AbilityResolved`, once per check. Iron Will, Install Upgrades, All Hands on Deck and Establish Network act
  through that record.
- "Investigative Committee" is played before the check. `SkillCheckView.investigated()` is true once the revealed
  cards played before it hold one (they stay with the check until it ends). `PlayCardsIntoSkillCheckEventProcessor`
  then records `SkillCheckFact.CardsShown(playerNumber, cards)` after the cards go into the pile like any other, so
  everyone learns who played what (`SkillCheckProjection.shownCards`, the turn panel's "Face up" rows). The Destiny
  cards are dealt face down before anyone plays.
- `EndSkillCheckEvent` runs after everything the result queued: it discards the cards of the check, the ones played
  before it too, and records `SkillCheckEnded`, which clears `GameView.activeSkillCheck()`.
- `DetermineSkillCheckResultEventProcessor.difficulty(game)` is the difficulty so far: Declare Emergency, Jury Rigged,
  4 less at the "Airlock" on a character in the "Brig", and what the Quorum Cards in play changed
  (`SkillCheckView.difficultyAdjustment()`, `SkillCheckFact.DifficultyAdjusted`).
- The Quorum Cards in play act in a check (see "Quorum Cards"): "Accept Prophecy" and the Arbitrator
  (`UseArbitratorEvent`) as the "Admiral's Quarters" is activated, the Chief of Staff (`UseChiefOfStaffEvent`,
  `SkillCheckView.politicsCountPositive()`) before any card goes in, and "Probation" (`UseProbationEvent`, a
  `CardsLookedAt` only the President sees) after its holder put cards in, which are then the top of the pile.
- The game board skill checks are the "Admiral's Quarters", the "Brig" and the "Airlock" (rule 10.3.3). The Daybreak
  "Administration" has no skill check: it draws a Mutiny Card, and `ReplaceMutinousPresidentEvent` then lets the
  player choose a new President if the President holds one. With "Accept Prophecy" in play the President is first
  asked whether to discard it and keep the title (`DefendPresidencyEvent`), and while a Vice President isn't President
  he takes the title without a choice. `SkillCheckView.targetPlayerNumber()` is the character the location check is
  about, the player himself at the "Brig".

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
`ResolveCapricaCrisisCardEvent` for the one a Cylon chooses at "Caprica", both through `CrisisCardReveals` (`drawn`:
"Delusional Intuition" for the player who drew it, `revealed`: "Secret Destiny" before the card resolves). The deck has
141 cards (the Cylon attack cards are left out, rule A2.7.2): the 136 different ones and the base game's extra copies of
"Food Shortage" and "Water Shortage" (`DecksHolder`). Every one resolves: the Cylon Fleet crises, the base game,
Pegasus, Exodus and Daybreak.

A `CrisisCard` constant lists what is printed on the card: its `SkillCheckType` (`null` for an event crisis), its
`CrisisChooser`, its `CylonActivation` icon and whether it has the "prepare for jump" icon. The images in
`materials-sorted/CrisisCard` are the source. An event crisis always has a chooser, and a skill check crisis has one
(`CURRENT_PLAYER`, `PRESIDENT`, `ADMIRAL` or `CAG`) when it gives an alternate instruction to carry out instead of the
skill check (rule 8.5.1.1). A skill check crisis without a chooser starts its check straight away.

`CrisisCardFollowups` (in `event/crisis`) holds the effects, grouped like `SkillCheckResultFollowups`. A card has two
sections, `TOP` and `BOTTOM`. On a skill check crisis the top is the skill check and the bottom the alternate
instruction.

- `choice(card)` gives a `OneOf` of two `ChooseCrisisSectionEvent`s, one per section. They are `PlayerEvent`s that name
  the chooser, because the engine derives who answers a `OneOf` from its options (see "Engine") and
  most sections name no player. Don't put the effects of a section directly into the `one(...)`. A section a card
  forbids isn't offered: the "nuke token" sections of "Abandon Galactica" and "Build Cylon Detector" need a nuke token.
- `ChooseCrisisSectionEventProcessor.resolve` starts the skill check on the top of a skill check crisis, and otherwise
  asks `followup(card, section)` for the effects of the section. When "Moral Compass" can change the choice, the
  processor offers `one(MoralCompassEvent, ResolveCrisisSectionEvent)` first, which resolve through the same method.
- `choice(card)` applies "Amoral" (only the first section for Tory Foster as current player) and adds "Choose a
  Different Path" (`ChooseADifferentPathEvent`, then `differentPath()`) for a chooser who can use it. Each builder
  serves one card, and gets Romo Lampkin's discards from `DeceitfulDiscards` ("Deceitful": his first is 1 card
  smaller, a `DeceitfulEvent`), as `SkillCheckResultFollowups` does for a Crisis Card's check. Every discard it gives
  is a forced one, which "For Love" may make smaller (`ForcedDiscards`, see "Character Abilities").
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

### Super Crisis Cards

A revealed Cylon draws one when he reveals (not on an execution) and with the "Resurrection Ship"'s action (no limit),
and plays one with the first option of "Caprica" (`CapricaPlaySuperCrisisCardEvent`, `event/location/cylon/`).

- `SuperCrisisCard` lists what is printed: its `SkillCheckType`, or for "Fighting Blind", "Psychological Warfare" and
  "Demand Peace Manifesto" the title holder who chooses (`CrisisChooser`). None has an activation or jump icon, and
  there is no Activate Cylon Ships step after it.
- The play takes the card out of the hand (`PlayerOps.playSuperCrisisCard`) and queues
  `ResolveSuperCrisisCardEvent(playerNumber, card)`, then `DiscardSuperCrisisCardEvent`, which leaves a card kept in
  play out of the discard pile. The resolve starts the skill check with `SkillCheckOps.startSuperCrisis`, which records
  who played it (`SkillCheckFact.PlayedBy`, `SkillCheckView.playedBy()`), or offers the chooser
  `ChooseSuperCrisisSectionEvent`s, whose effects are in `SuperCrisisCardFollowups`. The skill check results are in
  `SkillCheckResultFollowups`, with the Crisis Cards'.
- `SkillCheckType.isSuperCrisis()`: the character abilities that affect Crisis Cards or skill checks can't touch these
  (rule 6.1.2.5).
- "Human Prisoner"'s fail lets the Cylon who played it pick a human (`HumanPrisonerTakeHandEvent`), who gives him his
  Skill Cards and goes to "Sickbay". "Psychological Warfare" rebuilds the Destiny deck from 6 Treachery cards
  (`ReplaceDestinyDeckEvent`, as many as are left).
- "Bomb on Colonial One" kept in play destroys Colonial One (rule 9.4, `GameView.colonialOneDestroyed()`,
  `isDestroyed(location)`): nobody moves there, its locations have no action, and a new character whose start is
  there starts in "Sickbay" (rule 7.4, `PlaceInSetupLocationEventProcessor.startLocations`). "The Farm" kept in play blocks the humans' miracle
  abilities (`CharacterAbilities.canUseMiracle`).

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
        single(new NoOpEvent(playerNumber)),
        all(new DecreaseJumpPreparationEvent(), new DecreaseJumpPreparationEvent()));
```

### Event Constraints

`EventConstraint` is a plain enum of extra rules that can be pinned onto an event (e.g. `DRAW_EXACTLY_2`,
`DRAW_EXACTLY_2_OF_ANY_COLOR`, `DISCARD_EXACTLY_4`, `DISCARD_DOWN_TO_HAND_LIMIT`, `DISCARD_DOWN_TO_3`,
`DISCARD_DOWN_TO_1`, `MUST_LAUNCH_IN_VIPER`, `DESTINATION_GALACTICA`), and `drawExactly(n)`, `discardExactly(n)` and
`discardDownTo(1 or 3)` give
the counted ones. A processor attaches them to a
`PlayerDecisionEvent`, e.g. `new PlayerDecisionEvent<>(n, DrawSkillCardsEvent.class, DRAW_EXACTLY_2)`. When the
decision is answered, the engine copies them onto the submitted event with `ConstrainedEvent.withConstraints`,
replacing whatever the client sent, so a client can't drop one.

An event that supports a constraint implements `ConstrainedEvent` and has its own record component for it (e.g.
`DrawSkillCardsEvent.drawLimit`, `null` when there is none, or `LaunchViperEvent.mustLaunchInViper`). Its processor
checks the constraint in `isValid()` and throws `FrakCallTheAdmiralException` for any constraint it doesn't support.
`DrawSkillCardsEvent`, `DiscardSkillCardsEvent`, `DiscardQuorumCardsEvent`, `LeaveBrigEvent`, `LaunchViperEvent` and
the four `AnywhereLocationEvent`s (`ANYWHERE_EVEN_IF_DAMAGED`) implement it.

### Validation

`isValid()` is the only rule check a submitted event gets, so a processor checks everything the client sent: that the
players, ships and cards it names exist and are where they have to be, and the rules of the action. A queued event goes
through the same check, and failing it there is a bug. So a rule that only binds a player's choice can't go in
`isValid()` when the event is also queued directly. The processor that asks offers the legal options as a `one(...)`
instead, e.g. `FollowupUtils.chooseCharacterForSickbay` (rule 9.3.2).

- Location action processors extend `LocationActionProcessor`. Its final `isValid()` checks that the player is at
  `event.location()` and that `LocationEvent.canAct` allows the action (for an "Unorthodox Plan" only
  `canActIgnoringDamage`, see "Actions and Skill Cards"), then calls `isValidAction()` for the details of the event,
  and its final `process()` calls `act()`. `LocationEvent.canAct` is an exhaustive switch over `Location`, so a new
  location doesn't compile until it's mapped. A damaged location has no action (rule 11.7.2.2), and a location with
  preconditions asks the static `canAct` of its processor, e.g. FTL Control out of the red zone. The character bans
  and costs come first in `canActIgnoringDamage` (`CharacterBans.canActivate`, `ActionCosts.canPayLocationCost`), so
  they bind every way of activating a location (see "Character Abilities").
- A rule that another processor or a followup needs too is a public static check on its processor, e.g.
  `SendToBrigEventProcessor.canBeSentToBrig`, `MakePresidentEventProcessor.canBecomePresident` or
  `MakeAdmiralEventProcessor.canBecomeAdmiral` (and `MakeCagEventProcessor.canBecomeCag`: not in the "Brig").
- `EventProcessor.isPlayer(n)` checks a player number from the client, and `holdsSkillCards(cards)` the hand. It counts
  copies, because the decks hold equal cards.
- A decision must always have a legal answer, or the game waits forever. A processor queues one only when an answer
  exists, and an optional one goes in a `one(decision, NoOpEvent)`.
- A Skill Card color with no cards in its deck and discard pile can't be drawn (rule 8.2.2.5,
  `DecksHolderView.skillCardsLeft`). `DrawSkillCardsEvent` may draw fewer than its `DRAW_EXACTLY_n` only when no other
  card fits, and fixed-color draws take what is left.
- Clients may send null fields, so a check that would throw on one rejects it instead (the state's `List.copyOf`
  lists throw on `contains(null)` too).
- A followup is built before the events ahead of it run, so a queued event may find its character changed: an event
  that names him tolerates it, e.g. `SendToBrigEvent` leaves a character already in the "Brig" or a revealed Cylon
  where he is, `MakeAdmiralEvent` gives no title to one sent to the "Brig" meanwhile, and `AttackCylonShipResultEvent`
  misses once its fighter or target is gone (queued before the roll, whose cards and abilities can move them). A rule that binds a player's choice goes in the event he answers with, e.g.
  `ChooseCharacterForBrigEvent` (rule 9.2.3, `ChooseCharacterForBrigEventProcessor.offer`). An offer that depends on
  what the effects before it did is an event that builds it when it runs, e.g. `OfferPlayOrPassEvent`,
  `OfferUnmannedViperActivationEvent` (the CAG's activation after "Review Camera Footage" damaged the "Reserves",
  "Command"'s second), `OfferPilotLaunchEvent` ("Return to Duty"), `DutifulAgainEvent` and
  `ResolveCrisisSectionThenEvent` (the second part of a Crisis Card's
  section, from `CrisisCardFollowups.secondPart`).

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
  runs it, puts the results of `WinChecks` and `AbilityTriggers` ("Memento") before the followup, closes the scope with
  them all and pushes them. It stops at a
  `PlayerDecisionEvent` or a `OneOf`, and clears the agenda once the game has an outcome. Only that empties the
  agenda: running out of events before the game ends throws `FrakCallTheAdmiralException`.
- The chooser of a `OneOf` is the player of the `PlayerEvent`s in its options, otherwise the current player. That
  works because `PlayerEvent.playerNumber()` is always the acting player: an event that names a target, such as
  `SendToBrigEvent`, is a plain `Event`. Options that name different players are a bug.
- Input check: the right player, `decision.action().isInstance(event)` (so `ActionEvent` takes any location action),
  and a submitted `PlayerEvent` names the submitter.
- Rejected input throws `InvalidActionException` and changes nothing. A queued event that fails validation and a
  decision with constraints its event can't carry throw `FrakCallTheAdmiralException`. Every concrete event class has to
  have a processor, and `ProcessorRegistry.create` asserts it, also without asserts (`ProcessorRegistryTest`).
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
  randomness after a restore. `pendingInput()` is the pending input in full, a secret one included, and
  `pendingScope()` the `ScopeOpened` of the event that queued it (the agenda frame's parent, `Journal.scope`).
- Sandbox: `inject(SandboxEvent)` runs a setup event at once, ahead of the pending input, through the same run loop
  (win checks included). `JournalEntry.EventInjected(event)` before its scope tells `AgendaReplayer` to push it, with
  the root as its parent. `submit` never takes a `SandboxEvent`. `undo()` goes back to the pause before the last input:
  the engine knows where each input began (live from `advance`, after a restore from
  `AgendaReplayer.replayWithInputs`: the scope that answered a decision, each `ChoiceMade`, each `EventInjected`), and
  rebuilds from that prefix. A choice that led to a decision is undone on its own; before the first pause there is
  nothing to undo.
- Anytime input: an `AnytimeEvent` (a `PlayerEvent`, e.g. `RevealMotiveCardEvent`) is something a player may do at any
  pause without being asked. `submit` takes one from its own player at any pause, a choice or a secret decision too,
  and runs it ahead of the pending input as `inject` does (`runAhead`, recorded as `EventInjected`), so restore and undo
  treat it as one input. It answers nothing: the pending input stays as it was. A finished game, an event that names
  another player or fails `isValid()` is rejected and changes nothing. `LegalAnswers.anytime(game, n)` lists the ones a
  player may submit now, which the projection carries (see "Projections").
- Only answers: an engine after `takeOnlyAnswers()` (the backend's, `frakkin.take-only-answers`) answers a decision
  whose `LegalAnswers.only()` is present itself, as the run loop reaches it: one concrete answer, or one shape that
  takes every element of its pick (by position, so two equal cards are two ways and he is asked). A search that reported
  a crash or an unsupported class asks. `JournalEntry.OnlyAnswerTaken` comes before the answer's scope, which is logged
  under the decision's player; `AgendaReplayer` counts it as no input, so undo goes back past it. A choice is always
  asked: `one(...)` collapses a single option, so a choice of one is built to ask (`OfferPlayOrPassEvent`, so nobody
  learns that he holds no card). It applies from the next input on: the pause it stands at stays, so a restore
  changes no journal.
- `event/sandbox/` holds the `SandboxEvent` marker and the setup events, plain `Event`s with typed fields (one per kind
  of card): put a Crisis, Destination, Skill or Destiny card on top, give a Skill Card, a Mutiny Card, a Super Crisis
  Card or a Loyalty Card (`GiveLoyaltyCardToPlayerEvent`: from the Loyalty or the "You Are Not a Cylon" deck, or a "You
  Are a Cylon" card nobody has), give the President a Quorum
  Card, move a character, place a ship,
  change a resource, set the jump preparation track, damage a location, assign a title. Their processors use the
  rules' ops where one fits, and `SandboxOps` (`GameOps.sandbox()`) for the rest. `DeckFact.CardsPulled` takes a
  stacked or given card out of its draw pile, so no copy appears.

### Legal answers

`LegalAnswers.of(game, decision)` (`engine/answer/`) lists the answers a pending decision takes, for the player it
asks. It adds no rule: every candidate comes from the game as it is and its processor's `prepare` judges it, as it
judges a client's input.

- `LegalAnswers(events, shapes)`: an event is an answer `submit` takes as it is, with the decision's constraints on it.
  A `ShapedAnswer(example, pick)` has a part to pick: the example is legal, and the `Pick` (`SkillCardPick`,
  `QuorumCardPick`, `ShipPick`) names the component, the elements to pick from by position, `min` and `max`, and
  whether the order counts. A move's discard is a pick of one card. `covers(event)` says whether an event is one of
  them (a list compares as a multiset), `forOption(game, option)` gives the answers of the decision an option of a
  choice asks first (`decisionOf`), `answerTypes(action)` the classes that may answer, and `anytime(game, n)` the
  `AnytimeEvent`s a player may submit now, as the answers of a decision on `AnytimeEvent` itself.
- `AnswerClasses`: the records that may answer an action (not a sandbox event, and an anytime event only for the action
  `AnytimeEvent`), each skipped when the
  check the game asks before it offers one says no (`SkillCardPlays.canPlay`, `QuorumCardPlays`, `MutinyCardPlays`,
  `CharacterActions`, `CharacterMovements`, `LocationEvent.canAct`, `canActIgnoringDamage` with
  `ANYWHERE_EVEN_IF_DAMAGED`).
- `Domains`: the values of a scalar component, by type and name: the asked player for `playerNumber`, every player
  for another one, the ships of the kind the name says (`civilianShipId`, `raiderId`, `basestarId`, `cylonShipId`,
  `viperId`/`fighterId`, `centurionId`; any ship for `shipId`), the cards of the pile an enum comes from (his hand,
  the President's hand while he is President, the turn's Crisis Cards), every constant of any other enum, the
  activations of every fighter in space, and null for every reference. A skill check's cards count once they are
  divided, face up.
- `Shapes`: where a list's elements come from (the hand, the President's hand, the top Quorum Cards for
  "Influential", the ships of one place and kind), or the few list values to try as they are (the color selections of
  a draw, up to the constraint's count; enum lists of up to 2; the tiebreak of the cards before a check). The search
  judges a witness per size (the first and the last elements), so a source puts its elements in the order that makes
  the first ones legal: the strongest first for "Selfish" (`minStrength` 20), a Reckless card last before a check.
  Size 0 is a concrete answer, each run of legal sizes a shape, and a shape that another holds is dropped. A record
  with a list and no source is reported, and `LegalAnswersTest` lists the two that only a choice offers.
- A candidate whose check throws is left out, not a crash of the table; `LegalAnswers.Listener` gets it, and the
  classes the search has no values for. `LegalAnswersTest` checks a decision of each kind (every answer is taken, a
  known one is there and an illegal one is not), `LegalAnswersEngineTest` the scripted turns and an interrupt window.

### Turn Loop

The setup and the turns are ordinary events in `event/turn/`, so the engine knows nothing about steps. Each is a plain
`Event` about the current player.

- `StartGameEvent` follows rule A3.1 for the Kobol objective: `ops.startGame()`, `ops.galactica().setUp()`, a random
  first player (`GameOps.chooseFirstPlayer`), then queues `CreateDestinyDeckEvent`, the `SelectCharacterEvent`
  decisions clockwise from the first player, the three `DetermineNext...` events, `CreateLoyaltyDeckEvent`,
  `ReceiveStartingSkillsEvent` (everyone but the first player) and the first `StartTurnEvent`. A 7 player game needs
  one Cylon Leader, a 3 player game none (rule A3.7.2.2): the last seat to choose must take the Leader if nobody has.
  Boomer and Athena may both play (rule 6.2.4); the alternate versions are Lee, Tom Zarek, Helo and Baltar.
- `StartTurnEvent` records the turn (`GameOps.startTurn()` counts it for the game and the current player) and queues
  one event per step, then `EndTurnEvent`: `ReceiveSkillsStepEvent`, `MovementStepEvent`, `ActionStepEvent`,
  `CrisisStepEvent`, `ActivateCylonShipsStepEvent`, `PrepareForJumpStepEvent`, `DiscardStepEvent`.
- Each step records `StepChanged` and applies its own exceptions. A step a player doesn't have records nothing: revealed
  Cylons (and Cylon Leaders who aren't Infiltrating, see `isHuman()`) have no Crisis and no Activate Cylon Ships step,
  and characters in the "Brig" no Crisis step. Movement and Action are a `OneOf` with a `NoOpEvent`. The Action step
  offers what `Actions.offerAction` finds (see "Actions and Skill Cards"): an `ActionEvent` decision when the player can
  take his location's action or another one (`Actions.canTakeOtherAction`: an "Action:" Skill Card, a Quorum Card, a
  Mutiny Card, a Title Card's action, the Cylon reveal, a Personal Goal's reveal, an Infiltrating Cylon Leader's
  return, an "Action:" of his character sheet), and for a pilot an
  `ActivateViperEvent` decision too. A
  character with neither (e.g. in "Sickbay" without a card, see `LocationEvent.canAct`) gets no Action choice. The
  Movement step offers what `Actions.offerMovement` finds: a `MoveEvent` decision when he can move (not from the
  "Brig"), and a `MovementAbilityEvent` decision when he can play a "Movement:" card or use a "Movement:" ability of
  his sheet, in the "Brig" too (rule 10.4.1). The Movement and Action steps queue `EndOfMovementStepEvent` and
  `EndOfActionStepEvent` after the player's choice, only when he has an ability that acts then (see "Character
  Abilities").
- `DiscardStepEvent` asks every player over the hand limit, clockwise from the current player, with
  `DISCARD_DOWN_TO_HAND_LIMIT`: Skill Cards over `handLimit()`, and the President's Quorum Cards over
  `TitleOps.QUORUM_HAND_LIMIT` (10 for every character, rule 4.4.1.1).
- The Crisis step draws a card, keeps it with the turn and queues `ResolveCrisisCardEvent` through `CrisisCardReveals`
  (see "Crisis Cards"); Laura Roslin draws 2 and picks one (`ReligiousVisionsEvent`, the other to the bottom). The
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

### Actions and Skill Cards

`event/action/` knows what a player can do now, for the Action step and everything else that gives an action. Every
"Action:" and "Movement:" Skill Card is built, "Popular Influence" with the Quorum Cards
(see "Quorum Cards"): its player gives one drawn Quorum Card to the President and holds the other
(`PlayerPile.QUORUM`) to play it, through a decision on that card's event, or discard it.

- `Actions.canTakeAction` is "his location's action (`LocationEvent.canAct`) or another one" (`canTakeOtherAction`:
  a playable "Action:" Skill Card, a Quorum Card, a Mutiny Card, a Title Card's action through `TitleActions.canAct`,
  the Cylon reveal through `RevealCylonEventProcessor.canReveal`, a Personal Goal through
  `RevealPersonalGoalEventProcessor.canReveal`, the Infiltration Card's return through
  `ReturnToResurrectionShipEventProcessor.canReturn`, an "Action:" of the character sheet through
  `CharacterActions.canUseAny`),
  and
  `offerAction(game, n)` builds the choice: `one(decision<ActionEvent>, NoOpEvent)`, for a pilot
  `one(decision<ActivateViperEvent>, decision<ActionEvent> when he has another action, NoOpEvent)` (rule 11.2.1). A location
  event fails for a pilot, who is in space. `ActivateViperEvent` isn't an `ActionEvent`, because a non-pilot's one
  activates an unmanned viper.
- A Skill Card played for its ability is a `SkillCardEvent` (`playerNumber`, `card`), one record per card in
  `action/skillcard/`, which implements the interface of its timing: `ActionEvent` for "Action:",
  `MovementAbilityEvent` for "Movement:" (used instead of moving, once a turn, rule 8.3.5). The engine's
  `isInstance` check then accepts it only where that timing is asked. `SkillCardType.isAction()` and
  `isMovementAbility()` list the cards, and `SkillCardEventsTest` checks that each has one event.
- Its processor extends `SkillCardEventProcessor`, whose final `isValid()` checks the card's `type()`, the hand and
  `isHuman()` (revealed Cylons and Cylon Leaders who aren't Infiltrating can't, rules 6.1.2.3, 6.2.1), then
  `isValidPlay()`, and "Specialized" and "Reluctant" (`CharacterBans`, `ActionCosts`). Its final `process()` plays the
  card (`PlayerOps.play`: `CardsLost`, `SkillCardPlayed`, which `TurnView.playedSkillCards()` keeps for the cards
  limited per turn, and the discard) before `resolve()`.
- A card is offered and accepted only when its effect can happen, as a location's action is. `SkillCardPlays.canPlay`
  is an exhaustive switch over `SkillCardType` that asks each card processor's static `canPlay`, so a new type doesn't
  compile until it's mapped.
- "Critical Situation" offers `Actions.offerAction` after the card left the hand. It and "Executive Order" share a
  limit of 1 a turn, read from `playedSkillCards()` (`CriticalSituationEventProcessor.criticalSituationOrExecutiveOrderPlayed`).
- "Preventative Policy" records `LossReductionAdded`. Every loss goes through `AbstractOps.loseResource` (the
  `GalacticaOps` losses, the FUEL and FOOD damage tokens, destroyed civilian ships), which uses the reduction up on the
  next loss of that resource (`LossReduced`). The sandbox's `ChangeResourceEvent` uses `SandboxOps.decreaseResource`,
  which isn't a loss.
- `GrantEvent(playerNumber, grant, grantedBy)` lets a player act later: `ACTION`, `MOVE` (a plain `MoveEvent`, no
  Movement abilities outside your own Movement step, rule 8.3.5) or `MOVE_OR_ACTION`. It is queued, never submitted,
  makes the player the acting player when it runs (so grants nest), and offers through `Actions`. `grantedBy` brings
  its card's rules: an "Executive Order" grant does nothing for a Cylon player or once the turn ended early (A2.3.8.4,
  A2.3.8.6), a "State of Emergency" one always resolves. `ResetActingPlayerEvent` ends the card.
- "Executive Order" targets another human who can move or act, and queues `one(all(MOVE, ACTION), all(ACTION,
  ACTION))` (the first only when he can move), which he chooses because the grants name him. "State of Emergency"
  loses 1 food, then grants `MOVE_OR_ACTION` to every player clockwise from its player, Cylons included.
- The "Hangar Deck"'s "1 more action" is an `ACTION` grant, offered once the pilot is in space.
- Repeated viper activations ("Maximum Firepower", "Full Throttle"'s attack, "Combat Veteran", "Launch Reserves") go
  through `ViperActivationRoundEvent(playerNumber, viperId, activationsLeft, attacksOnly)`, queued with the viper the
  card decided. It offers `ActivateViperEventProcessor.activations` of that viper as concrete
  `GrantedViperActivationEvent` options, each followed by the next round, and a `NoOpEvent` to stop, so a client
  answering by index can't switch vipers or add an activation. A `GrantedViperActivationEvent` is never a decision's
  answer, and takes the player's own viper or an unmanned one (rule 11.1). After a launch from the "Reserves" the
  viper is the one `GalacticaOps.launchHumanFighter` takes: the lowest id of its type.
- "Launch Scout" and "Scout for Fuel" need a raptor in the "Reserves" (rule 13.5) and roll through
  `FollowupUtils.rollDie` with a result event in `action/skillcard/`. A scout looks at a card through
  `LookAtTopCardEvent(playerNumber, deck)` (`event/deck/`, `CardsSeen`, then top or bottom). "Test the Limits" plays
  only before the blue spaces of the jump track (`MINUS_3`, `MINUS_1`, as printed) and damages Galactica like every
  other effect (see "Combat").
- "Raptor Specialist" builds an assault raptor from `GalacticaBoardView.supply()` (ship zone `HUMAN_SUPPLY`: ids 22 to
  24, 4 assault raptors in all, A2.4.4) for a raptor from the "Reserves", or returns a destroyed raptor.
- "Repair" repairs the player's damaged location, or at the "Hangar Deck" up to 2 damaged vipers (`ShipOps.repair`, to
  the "Reserves"). `BattlestarOps.repairLocation` records `LocationRepaired` and puts the location's damage token back
  on its pile, shuffled (rule 11.7.2.2). Not every location has a token (`DamageTokens.of`), and the sandbox's
  `DamageLocationEvent` pulls the token out of its pile (`CardsPulled`), as a draw would, so a repair returns one copy.
  `RepairEventProcessor.isDamaged`, `areDamagedVipers` and `repairLocation` are shared with "Resources for Galactica".
- "Unorthodox Plan" takes the action of "Command", "Armory", "Weapons Control" or "Communications" from anywhere, even
  damaged. It queues the location's decision with `ANYWHERE_EVEN_IF_DAMAGED`. Those four events are
  `AnywhereLocationEvent`s with an `anywhere` component, which only that constraint sets: the engine replaces a
  submitted event's constraints with the decision's, so the Action step's own decision clears it.
  `LocationActionProcessor` then checks `LocationEvent.canActIgnoringDamage` (the location's own rules) instead of the
  location and the damage.
- "Negotiation" places a basestar (`PlaceShipsEvent`: placing isn't launching), draws a Politics card and records
  `CylonShipsHeld` (`TurnView.cylonShipsHeld()`, until the next `TurnStarted`). Every processor that launches or
  activates Cylon ships (`ActivateRaidersEvent`, `ActivateRaiderEvent`, `LaunchRaidersEvent`,
  `ActivateHeavyRaidersAndCenturionsEvent`, `ActivateCylonBasestarsEvent`, `ActivateCylonBasestarEvent`) does nothing
  while they are held, so a Crisis Card's activation stops too, and the "Cylon Fleet" location has no action then.

### Quorum Cards

Every Quorum Card can be played. They follow the Skill Cards' pattern, with the card's choices
on its event.

- A play is a `QuorumCardEvent` (an `ActionEvent`), one record per card in `action/quorum/`. The record is the card: it
  has `playerNumber` and the card's choices, no card component, and its processor's `card()` names it.
  `QuorumCardEventProcessor`'s final `isValid()` checks that the player is human and holds the card (his own held card
  first, otherwise the President's hand), then `isValidPlay()`. Its final `process()` takes the card out of where it
  was, then `resolve()`.
- `QuorumCardPlays.canPlay` is an exhaustive switch over `QuorumCard` that asks each processor's static `canPlay`: a
  card is offered only when its effect can happen. `canPlayAny` covers the President's hand, and `eventClass(card)`
  gives the event of a card, for a decision that takes only that card.
- Who plays: the President, as his action (rule 8.4) from his hand, also from the "Brig" and as a pilot; the President
  at the Quorum Chamber, which draws a card and then offers `one(DrawQuorumCardEvent, decision<QuorumCardEvent>,
  NoOpEvent)`; and the player of "Popular Influence" (see "Actions and Skill Cards").
- `PresidentTitleActionEvent` (`action/title/`) is the President Title Card's action: draw a Quorum Card. The
  Admiral's and the CAG's are beside it (see "Combat").
- A played card is in no pile while it resolves and goes where its text says, usually last: the discard pile
  (`DiscardPlayedQuorumCardEvent`, face up), out of the game (`DeckFact.CardsRemovedFromGame`, into
  `GameView.removedComponents()`), or in play. A card whose fate depends on a roll settles it in its result event.
- The deck holds 29 cards (`DecksHolder`): the second "Authorization of Brutal Force" is inferred, check the physical
  deck. The hand limit is `TitleOps.QUORUM_HAND_LIMIT` (10) for every President. `DecksHolderView.quorumCardsLeft()`
  counts the draw and discard piles; nothing is drawn when both are empty.
- In play (`GameView.quorumCardsInPlay()`, `QuorumCardPutInPlay`, `QuorumCardLeftPlay`, public): the four "Assign"
  cards and "Probation" with their holder, "Accept Prophecy" without one. A card used up leaves play
  (`TitleOps.discardFromPlay`), and the cards given to a player go on execution and at the end of an Infiltration
  (`TitleOps.discardQuorumCardsGivenTo`).
- Where they act: the Arbitrator and "Accept Prophecy" at the "Admiral's Quarters", the Chief of Staff and "Probation"
  in skill checks (see "Skill Check Sequence"), the Mission Specialist at the jump and the Vice President and "Accept
  Prophecy" at the "Administration" (see "Jump and Kobol" and "Skill Check Sequence").
- The sandbox gives the President a card with `GiveQuorumCardToPresidentEvent`. The wire schema names each Quorum
  event's `quorumCard`, by which the answers and the log name the card played. The mats show the cards in play face up
  in front of their holder.

### End of Game and Titles

- `WinChecks` (`event/endgame/`) holds the Cylon wins that happen at once (rule 16.1): a centurion at the end of the
  Boarding Party track, and 6 damaged Galactica locations. The engine asks after every event, and `ScopeClosed` records
  the result with the event's followup, so restore never runs the checks. The resource check stays in `EndTurnEvent`.
- `CylonsWinEvent` ends the game for the Cylons through `EndGame.end` (the `GameEnded` fact), and `GameView.outcome()` gives the winning `Team`,
  which `Finished(winner)` reports, and `GameView.winners()` the players who won. A test script that plays to the end has to end the game, e.g. with a
  `CylonsWinEvent`.
- Titles in the "Brig" (rules 4.4.2.3 and 4.4.3.2): `DetermineNextAdmiralEvent` and `DetermineNextCagEvent` pick the
  highest in line outside the "Brig", and when every human is in it, the highest in line there.
  `LineOfSuccessionSelector.nextInLine` skips revealed Cylons. Leaving the "Brig" (`LeaveBrigEvent`,
  `MoveFromBrigEvent`) queues them again while the Admiral or the CAG is still inside (`BrigTitles.afterLeaving`), so
  the one who left takes the title.
- The Admiral's and the CAG's Title Card actions are under "Combat", the President's under "Quorum Cards".

### Mutiny Cards

- To draw a Mutiny Card, queue a `DrawMutinyCardEvent`. Its processor sends the player to the "Brig" on the second card
  (the Mutineer on the third, `PlayerView.isMutineer()`), and asks for a `Discard1MutinyCardEvent` per card over the
  limit (rules 5.2.3.2 and 5.2.3.3). In the "Brig" and off the board (a Stranded Helo) the player only discards. Only
  the Press Room calls `PlayerOps.drawMutinyCard()`, because its target doesn't move. Discarding a "Bait" or a "Dradis
  Contact" by choice draws one once a turn per player (`TurnLimit.MUTINY_CARD_FROM_TREACHERY`, rule A2.3.8.1): every
  discard of cards the player picked asks `TreacheryDiscards.mutinyCardFor` (`DiscardSkillCardsEvent`, moving between
  ships, the "Engine Room", "Star Player", "Selfish", "Hotshot"), not a random one or the whole hand. A new character
  who keeps the Mutineer card after an execution draws one too (`FinishExecutionEvent`, rule 5.2.1).
- Every Mutiny Card is played as an action (rule 5.2.3.1), following the Quorum Cards' pattern: a
  `MutinyCardEvent` (an `ActionEvent`), one record per card in `action/mutiny/` with the card's choices on it, and a
  processor extending `MutinyCardEventProcessor`, whose final `isValid()` checks a human (an Infiltrating Cylon Leader
  counts, rules 5.2.3.4 and 5.2.3.5) holding the card, also in the "Brig", then `isValidPlay()`. Its final `process()`
  takes the card out of the hand (`PlayerOps.playMutinyCard`), then `resolve()` ends with its fate:
  `DiscardPlayedMutinyCardEvent` (face up) or `RemoveMutinyCardFromGameEvent`. `MutinyCardPlays.canPlay` is an
  exhaustive switch over `MutinyCard` asking each processor's static `canPlay` (the card's "can't play" line), and
  `MutinyCardEventsTest` checks one event per card.
- What they share: `PlayerOps.giveSkillCards` and `takeRandomSkillCards` ("Blackmail"), `PlaceShipsEvent`'s
  `cylonArea` ("Necessary Risk"), `LaunchRaidersEvent.fromEachBasestar(n)` (unlike the icon, nothing goes to the Cylon
  Fleet board without a basestar, rule 9.6), `ActivateUnmannedViperEvent` (a decision's answer: an unmanned viper even
  for a pilot, "Panic"), `ShuffleCivilianShipIntoDeckEvent`, `LookAtTopCardEvent` (also the "Human Fleet"'s look),
  `NukeResultEvent` ("Unauthorized Usage" discards every nuke, errata A2.3.3.2) and `TitleOps.putQuorumCardsAtBottom`
  ("Set the Agenda", after a `CardsSeen` of the President's hand). The sandbox deals one with
  `GiveMutinyCardToPlayerEvent`.

### Execution

`ExecuteEvent(playerNumber)` follows rule 7.1. Its player carries out the steps, so he is the player of the event.
`ExecuteEventProcessor.canBeExecuted` allows humans only: a revealed Cylon ignores it (rule 6.1.2.1).

- The player discards his Skill and Mutiny Cards, his miracle tokens, and the Quorum Cards given to him
  (`TitleOps.discardQuorumCardsGivenTo`, also for an Infiltrating Cylon Leader, rule 6.2.3.5).
- A human: lose 1 morale (`MoraleLostToExecution` when the loss happens, `GameView.moraleLostToExecution()`, which
  "Eulogy" asks for), the character leaves the game (`CharacterRemoved`, into `GameView.removedComponents()`), the
  Loyalty Cards are discarded but the Mutineer card stays, and the player chooses a new character. Each Final Five card
  among them makes every other human with Skill Cards discard 2 (rule 5.4.1.2), then leaves the game
  (`CardsRemovedFromGame(LOYALTY)`). Once the game is
  under way, `SelectCharacterEventProcessor.canBeSelected` allows any type but a Cylon Leader, and never an executed
  character or its other version. `FinishExecutionEvent` then adds a "You Are Not a Cylon" card to the Loyalty deck and
  deals one (`DealLoyaltyCardsEvent`, with extra cards for Boomer and Baltar before the Sleeper Agent phase), and hands on
  the titles the executed character held. No character left to choose: `CylonsWinEvent`.
- A hidden Cylon reveals a "You Are a Cylon" card through `RevealCylonOnExecutionEvent`, his choice when he holds two
  different ones, without its power, then follows `FinishCylonRevealEvent(n, false)`: no Super Crisis Card. An
  Infiltrating Cylon Leader's Infiltration ends (`PlayerOps.endInfiltration`). See "Cylons and Loyalty Cards".
- The executed current player's turn ends early (see "Turn Loop").
- Until he has chosen his new character the player plays none: `isHuman()` is false, so no effect on the humans
  reaches him, e.g. the discards of a Final Five card that another execution reveals in between.

### Cylons and Loyalty Cards

The Loyalty Cards' actions and effects, in `event/loyalty/`, and the Cylon Leaders.

- The voluntary reveal (rule 6.1) is an action: `RevealCylonEvent(n, card)`, for a hidden Cylon who isn't a Cylon
  Leader. It records `LoyaltyCardRevealed`, then follows rule 6.1.1 in this order: step 1 (all Mutiny Cards, the miracle
  token, the Quorum Cards given to him, and down to 3 Skill Cards through a `DISCARD_DOWN_TO_3` decision), the card's
  power (none in the "Brig"), then `FinishCylonRevealEvent(n, true)`: the titles first (`GiveTitlesToNextInLineEvent`),
  the "Resurrection Ship" (a piloted viper goes back to the "Reserves"), a Super Crisis Card, the other Loyalty Cards
  to a human (not from 7 distance on), the Mutineer card to a human, and the end of his turn if it is his. "Draw
  Treachery Cards" says its player discards down to 3 before he draws, hence the order.
- `CylonRevealPowers.power` gives each card's power as concrete `CylonPower...Event`s that name the revealer, in a
  `one(..., NoOpEvent)` for the "may" ones (all but "Draw Treachery Cards"). "Send to Brig" and "Send to Sickbay" target a
  character on a Galactica location (`CylonRevealPowers.isOnGalactica`). "Damage Galactica" draws up to 5 tokens and
  offers every pair as a `ChooseGalacticaDamageTokensEvent` (resolved, returned).
- Every look at another player's Loyalty Cards goes through `LookAtLoyaltyCardEvent` (1 random card) or
  `LookAtAllLoyaltyCardsEvent` (all of them, e.g. "Cylon Detector"), whose processors call `PlayerOps.lookAtLoyaltyCard`
  and `lookAtAllLoyaltyCards` (they return what was seen, `LoyaltyCardSeen` each) and stop while "Detector Sabotage" is
  in play.
- `FinalFiveCards.seen(looker, holder, cards)` turns the Final Five cards seen by another player (rule 5.4.1.1) into
  `ResolveFinalFiveCardEvent(holder, card, looker)`, several in the order the current player picks
  (`ResolveFinalFiveCardsEvent`: plain events, so he chooses). The holder shows the card to everyone
  (`PlayerFact.FinalFiveCardShown`, which applies nothing) and keeps it. The effects skip revealed Cylons: the Cylon
  ships activate by the card's icons (raiders, basestars, heavy raiders), Galactica is damaged twice, the looker is
  executed or sent to the "Brig", or the holder is executed (the card leaves the game with him, unresolved by that
  execution) and every other human discards 2 random Skill Cards. An execution resolves the Final Five cards it
  reveals (see "Execution").
- A player reveals a Personal Goal with `RevealPersonalGoalEvent(playerNumber, card, discards)`, an `ActionEvent`
  offered while its condition holds (`RevealPersonalGoalEventProcessor.canReveal`, rule 5.3.1; `discards` only for
  "Selfish", 20 strength). At 6 distance or less a "You Are Not a Cylon" card goes into the Loyalty deck and he is dealt
  a new card, as after an execution (`LoyaltyDeckOps.addNotACylon`, which adds nothing once that deck is empty). A
  revealed card leaves `loyaltyCards().cards()` for `revealedCards()`, so the end game charges only the unrevealed ones,
  and an execution discards the revealed ones too (`Deck.remove` takes revealed cards).
- `PlayerOps.endInfiltration()` ends a Cylon Leader's Infiltration for any reason (rule 6.2.3.5): `InfiltrationEnded`,
  back to the "Resurrection Ship", the Quorum Cards given to him and his Mutiny Cards discarded. He keeps his miracle
  token (rule 4.3.2). `ReturnToResurrectionShipEvent` (`event/player/`) is the Infiltration Card's action, which
  `Actions.canTakeOtherAction` offers; from the "Brig" it then asks for a discard down to 3 Skill Cards
  (`DISCARD_DOWN_TO_3`). A Cylon Leader who starts on a location of the humans starts Infiltrating there
  (`PlaceInSetupLocationEventProcessor.startAt`): Sharon "Athena" Agathon, and D'Anna Biers at the "Press Room".
- During the game a Cylon Leader reveals a Motive Card with `RevealMotiveCardEvent(playerNumber, card)`, an
  `AnytimeEvent` (see "Engine"), while its requirement is met (`MotiveCards.canReveal`: the cards that say "if the game
  is over" wait for the end, rule 6.2.2.1). A revealed card stays revealed, and `EndGame.end` reveals at the end what
  is left (see "Jump and Kobol").

### Jump and Kobol

The jump is a chain of events, one per step of rule 12.1.1. `JumpingTheFleetEvent` comes from the "FTL Control" action
and from `IncreaseJumpPreparationEvent` (the track has no "Auto Jump" space, the token goes back to `START`).

- `JumpingTheFleetEventProcessor` queues a decision per assault raptor in space (`one(ReturnAssaultRaptorEvent,
  NoOpEvent)`: its pilot, or the current player when unmanned, may keep it there), `ReturnVipersToReservesEvent(true)`,
  `MoveCylonShipsToFleetBoardEvent` and `DrawDestinationCardsEvent`. After 8 distance it queues `EndGameEvent` instead.
- The Admiral, or the Mission Specialist with 3 cards instead of 2 (his card then leaves play), picks a
  `ChooseDestinationEvent`, which names the chosen card and the others, which go to the bottom. `ResolveDestinationCardEvent`
  records `DestinationReached` (`GameView.destinations()` and `distanceTraveled()`), queues the card's effects
  (`DestinationCardFollowups`, the card text in its comments), `ObjectiveInstructionsEvent(distanceBefore)` and
  `ResetJumpPreparationEvent`. "Lion's Head Nebula" advances the track after the reset, "Misjump" draws its new card at
  once. `DestinationCard` has the printed distance, and "Mining Asteroid" is left out because the Crisis deck has no
  "Scar". "Legendary Discovery" counts as 1 distance too, through `crisisCardsInPlay()`.
- The cards that ask the Admiral: `RiskShipsEvent` (with `RiskResultEvent` after the roll, only when enough ships are in
  the "Reserves"), `GasGiantEvent`, the "Civilian Convoy" trade and `GasCloudPlaceEvent`. "Ragnar Anchorage" repairs as
  many ships as possible without asking.
- `ObjectiveInstructionsEvent` queues `SleeperAgentPhaseEvent` when the distance reaches 4 for the first time.
  It deals a card per player with `DealLoyaltyCardsEvent` (Motive Cards to a Cylon Leader), two to Boomer, whose "Sleeper
  Agent" then sends her to the "Brig" (`SleeperAgentEvent`) unless she is a revealed Cylon; Baltar's "Coward" card
  comes at the start of the game only. Then `FinishSleeperAgentPhaseEvent` has the revealed Cylons pass on the Mutineer card and hand their
  cards to a human, and the current player choose who draws an extra card when nobody got the Mutineer card. From 7
  distance on no Loyalty Cards are handed off (`FinishSleeperAgentPhaseEventProcessor.canHandOffLoyaltyCards`).
- `RevealMutineerEvent(playerNumber, drawsLoyaltyCard)`: the card stays among the Loyalty Cards and everybody knows it
  (the projections show it faceup). The player loses the titles, draws a Mutiny Card and, when dealt it facedown, another
  Loyalty Card.
- `EndGameEvent` follows rules 16.1 and 16.2: each unrevealed Personal Goal of a human reduces its resource by 1
  (`LoyaltyCard.personalGoalResource`), and the Cylons win if a resource is 0. Every ending goes through
  `EndGame.end(game, ops, team)`: the Cylon Leaders reveal the Motive Cards they can (`MotiveCards`, the requirements as
  printed, `MotiveCard.allegiance`), and `GameEnded` carries the winning players (`GameView.winners()`), the Leaders with
  at least two revealed cards of the winning allegiance and at most one unrevealed included (rule 16.3.2). The Motive
  Cards and Personal Goals revealed during the game are under "Cylons and Loyalty Cards".

### Interrupt Cards

Six Skill Cards are played in response to something, outside the Action step: "Strategic Planning", "Calculations",
"Evasive Maneuvers", "Run Interference", "Best of the Best" and "Major Victory". They are played in interrupt windows,
in `event/interrupt/`.

- `InterruptWindow` (`state/interrupt/`) is a sealed set of records, one per window, and `cards()` lists the card types
  each takes: before a die roll, before a piloted viper's attack roll, after a die roll, after the "FTL Control" roll
  (`AfterFtlControlRoll`, for "FTL Operator"), after a viper is attacked, before raiders activate, after a basestar or
  centurion is destroyed. `startsDieRoll()` and `followsDieRoll()` say where it stands to its roll.
- `OpenInterruptWindowEvent(window)` is an ordinary queued event. Its processor asks, as the hands are at that moment,
  each player who can play a card for the window, one at a time and clockwise from the current player
  (`PlayInterruptCardEventProcessor.canPlay`: humans only, rule 6.1.2.3, plus each card's own condition), then the same
  player again when he has a character ability for it (`RollAbilities.canUse`, see "Character Abilities"). The
  questions are decisions on `PlayInterruptCardEvent` and `UseRollAbilityEvent`, and no card or ability means a pass.
  Without holders the window doesn't open and the game doesn't pause.
- `PlayInterruptCardEvent` and `UseRollAbilityEvent` are `SecretEvent`s, so `PendingInput.secret()` is true while one
  is pending, and `GameEngine.status` shows it only to the player who answers. The others get a plain `Waiting`.
- `CloseInterruptWindowEvent` runs once everyone has answered. Two cards of a type conflict ("Run Interference" only in
  the same space area), and the current player keeps one, choosing among `TiebreakInterruptCardsEvent` options, one per
  way to keep a card of each conflict (rule 13.3): the options show him the cards. The other goes back to its owner's
  hand. Then each card resolves in play order (`InterruptCardResolved`, into the discard pile),
  then each ability (`RollAbilityResolved`, which records its use), and the window closes. One reroll a window: a
  reroll ability after an "Evasive Maneuvers" or another reroll is withdrawn (`RollAbilityWithdrawn`) and can be used
  in the window that opens again on the new roll. Windows never nest: a window closes before what its cards queue
  runs.
- Die rolls: `FollowupUtils.rollDie(result)` returns `all(window before, RollDieEvent, window after, result)`, and the
  result event reads `TurnView.dieRoll()`. The window before a roll starts a new `DieRoll` (`DieRollStarted`), and
  `DieRollCardPlayed(card, modifier)` records each card that takes effect on it. A reroll is another `DieRolled` and
  keeps the cards (rule A2.1.1.10). The result is clamped to 1-8 after all of them (rule 13.1). An `ArchitectureTest`
  rule lets only `RollDieEventProcessor` call `GameOps.rollDie()`. An ability that sets the face instead of the die
  (`DieRollReplaced`: "Hotshot", "Longshot") makes `RollDieEvent` roll nothing, and a fixed face ("Longshot") is the
  result as it is: no window opens after it. `DieRollAbilityUsed` records each ability on the roll
  (`DieRollView.abilities()`).
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
- `AttackGalacticaEvent(cylonShipId)`: a raider or a basestar. A hit is a `DamageBattlestarEvent`.
  `ActivateCylonBasestarEvent` queues it unless the weapons are disabled.
- Every effect that damages Galactica queues `DamageBattlestarEvent` (`FollowupUtils.damageGalactica()`), which
  decides when it runs: `one(DamageGalacticaEvent, DamagePegasusEvent)` for the current player while Pegasus is in
  play (rule 11.7.2), only Galactica once it is destroyed. The fourth damaged Pegasus location queues
  `DestroyPegasusEvent` (rule 11.7.2.4, also after the sandbox's `DamageLocationEvent`): everyone on Pegasus goes to
  "Sickbay" and `PegasusDestroyed` takes the board out of the game (`PegasusBoardView.destroyed()`). Its locations are
  then `GameView.isDestroyed`, as a destroyed Colonial One's are: no move, start (Helena Cain starts at "Command"),
  action or repair there, and the board veils them.
- `DamageVipersEvent(count, area)` damages one fighter at a time: the player taking an action picks it, the current
  player when nobody is (rule 11.3.2.2), from `DamageChosenViperEvent` options, one per kind of fighter in each place
  (a single kind needs no choice). An assault raptor counts as a viper and is destroyed (rule 11.3.2.3).
- `AttackCylonShipEvent(humanFighterId, cylonShipId)`: a fighter, or Galactica with "Weapons Control" (`byGalactica`),
  attacks a raider, a heavy raider or a basestar. A piloted fighter's roll gets the "Best of the Best" window. A hit on
  a basestar is a `DamageBasestarEvent`, which destroys it at 3 damage (a critical hit counts 2) and opens the "Major
  Victory" window. "Structural Damage" adds 2.
- `ActivateViperEvent(playerNumber, ViperActivation)`: one option of rule 11.1.1. A pilot activates his own viper,
  anyone else an unmanned one. `ProtectTheFleetEvent` takes the same `ViperActivation`, for an unmanned viper, and both
  use `ActivateViperEventProcessor.isValidActivation` and `activate`, and `activations` lists every legal one of a
  fighter in space. A pilot's action (`Actions.offerAction`, also
  for the "Hangar Deck"'s extra action) offers the activation as a decision, and "Command" only when
  `canActivateUnmanned`.
- The Admiral's nuke (`LaunchNukeEvent(playerNumber, area)`, `action/title/`, rule 11.6.1: a space area, not the Title
  Card's older "a basestar") spends a nuke and rolls. `NukeResultEvent` reads the roll: an 8 destroys every ship in the
  area (a pilot goes to "Sickbay", a civilian ship costs its resources), a 7 without a basestar still destroys 3
  raiders, otherwise a `NukeBasestarEvent(playerNumber, basestarId, result)` per basestar there, which the launcher
  picks: 1-2 damage it twice, 3-6 destroy it, 7 destroy it and 3 raiders. A destroyed basestar opens the "Major
  Victory" window.
- The CAG's Title Card actions: `PassCagTitleEvent(playerNumber, activation, targetPlayerNumber)` activates an
  unmanned viper, then gives the title to another human who can hold it (before what the activation brings resolves,
  e.g. "Alert Viper Pilot"'s action), and `CagPilotActionEvent(playerNumber,
  activation)`, once a turn (`TurnLimit.CAG_PILOT_ACTION`) while piloting, activates an unmanned viper, then grants
  another action (`GrantEvent(ACTION, CAG_TITLE)`). Both check the activation like `ProtectTheFleetEvent`.
- `DestroyCenturionEvent` destroys a centurion on the Boarding Party track and opens the "Major Victory" window (the
  "Armory" result, "Authorization of Brutal Force"). `DestroyHeavyRaiderEvent` destroys a heavy raider in space.
- Every effect that places or launches a Cylon ship on the main game board goes through `AbstractOps.takeFromSupply`
  (rule 11.4.2): a ship from the supply, and when it has none left, one from the lowest-numbered Cylon space area of the
  Cylon Fleet board. Without one there either, nothing is placed. A ship placed on the Cylon Fleet board with none left
  in the supply moves every ship of the highest-numbered Cylon space area holding one of its type to the main board
  instead (rule 11.4.1.2, `PlaceShipOnCylonFleetBoardEvent`), and nothing when there is none.
- The raiders of an activation activate one space area at a time, in the order the current player chooses (rule 11.5):
  `ActivateRaiderAreasEvent` offers the areas left as `ActivateRaiderAreaEvent` options, without a choice for one area.
  Basestars and heavy raiders activate clockwise, where the order can't change the outcome. A basestar with a "Disabled
  Hangar" launches no raider or heavy raider, wherever the launch comes from (`LaunchRaidersEventProcessor.canLaunch`,
  rule 11.7.3).

### Journal and Replay

The journal records exactly what the game ops did, so a game can be rebuilt from the journal alone. It is fact-sourced: replay applies recorded facts, and never re-runs processors or needs randomness.

- `Fact` (`engine/journal/fact/`) is a sealed interface, grouped by area: `BoardFact`, `DeckFact`, `GameFact`,
  `InterruptFact`, `PlayerFact`, `ShipFact`, `SkillCheckFact`, `TitleFact`. The records are nested in the area interfaces, because a
  sealed hierarchy can't span packages without a `module-info`. A new fact has to be handled in `FactApplier`'s
  exhaustive `switch`.
- `Journal` is an append-only flat list of `ScopeOpened(scopeId, parentId, event, actor)` / `FactRecorded` /
  `ScopeClosed(scopeId, followup)` / `ChoiceMade(scopeId, chooser, option)`, and the engine's `EventInjected` and
  `OnlyAnswerTaken` (see "Engine"). It isn't part of `Game`: `GameEngine`
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
4. **Fact fields are complete and immutable.** Cards are listed in full. Hiding them is the projections' job (see
   "Projections").
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
  player cards, the President's Quorum hand (the Quorum Cards in play are public), looked-at Loyalty Cards and civilian
  ship cargo, face down discard piles (`Disclosure.FACEDOWN_DISCARDS`), cards played before a skill check, an open
  interrupt window's cards (the others count them in the hand), the pending input (backend decision 6) with its
  `Topic` (`GameProjection.of(view, pendingInput, pendingScope, viewer)`: the event that queued it, a `SecretEvent` only
  to its actor and its fields only when public, as the log shows the scope) and with its legal
  answers (`Decide.answers`, and `Choose.answers`: an `OptionAnswers` per option that asks the chooser a decision at
  once, e.g. the action of the Action step), what a player may do at any pause (`anytime`: an `AnytimeAnswers` per
  player the viewer acts for who can do something, his own, a sandbox's every player's; empty once the game has
  ended), and everything once the game has ended. The Mutineer card is faceup once held ("immediately reveal this card").
- `LiveUpdateProjection.of(journal, from, viewer)` is what happened: the scopes (`ScopeLog`: actor, event type, its
  fields when public, the facts, and `automatic` for the only answer the engine took, which only its actor sees: that
  it was his only one can tell what he holds) and the choices (`ChoiceLog`). `LiveUpdateProjector` has an exhaustive switch over every
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
stores its journal in Postgres and pushes to every viewer what he may see. It adds no game rules.

- `journal/` - `JournalCodec` (a `JournalEntry` to JSON and back, `FORMAT_VERSION`), `ValueCodec` (records by their
  components; a value of an interface type carries its `kind`, from the closed `TypeRegistry`; a `List<?>` of cards
  takes its type from the `DeckId` or `PlayerPile` beside it, `cardType()`), `WireCodec` (the projections), and
  `TypeWalker` (every type and its components, for the format lock). `FormatLockTest` checks the format against
  `src/test/resources/journal-format.txt`: a change needs a new `FORMAT_VERSION`, then run it with
  `-DupdateFormatLock=true`. An event is named by its simple name, a fact with its area (`DeckFact.CardsDrawn`).
- `game/` - `GameSessions` runs the commands of a game one at a time (a lock per game): session from the Caffeine
  cache or restored from the rows, the state token (`StateTokens`, the sequence hashed with `frakkin.token-secret`),
  the engine, the new entries and the `game_bundle` summary in one transaction (a failed write evicts the session),
  then the projections (with the engine's `pendingScope()`) to the `ProjectionSink`, under the lock. A viewer for whom nothing visible changed gets nothing.
  Every engine it starts or restores takes only answers while `frakkin.take-only-answers` is on (the default, see
  "Engine"). `JournalStore`, `GameBundles` (with `mode` and `seed`: a seeded game restores with its seed).
- `user/` - logins through Google and Discord (`LoginProviders`, only those with a client id), the `dev` profile's
  login by name (`POST /api/dev/login`, refused together with a real provider), `AppUserAuthentication` in the HTTP
  session, which Spring Session keeps in Postgres so a login survives a restart, `GET`/`PUT /api/me`. CSRF through the
  `XSRF-TOKEN` cookie and the `X-XSRF-TOKEN` header.
- `lobby/` - `/api/games`: host, list (open lobbies, mine, running ones to watch), join as a player (next free seat, 7
  at most) or an observer, leave, and start by the host with 3 to 7 players (the seats close up to 1..n).
- `live/` - `/ws/games/{id}`: the handshake needs a participant and an allowed `Origin`, and gives the connection its
  `Viewer`. `ConnectionPool` is the `ProjectionSink`; each `LiveConnection` sends from its own queue. Client messages:
  `status`, `history`, `submit` (token, event: an answer to the pending decision, or an `AnytimeEvent`, which
  `GameSessions` runs ahead of it, with the player of the connection, the one it names in a sandbox), `choose` (token,
  option index). Server messages: `game` (token,
  projection), `update`, `log`, `rejected`. A new connection gets `game`, then `log`. A game that can't be opened
  (e.g. saved by another `FORMAT_VERSION`) gets `rejected` (`UNAVAILABLE`, with the reason) and then the close 1008
  (`LiveConnection.closeAfterSending`), after which `GameSocket` doesn't reconnect; the table shows the reason, and the
  room's `unavailableReason` before any socket.
- `sandbox/` - `POST /api/sandbox {playerCount, seed?}` starts a sandbox (`game_bundle.mode = SANDBOX`, a seeded
  engine, the caller its only participant, with the `Sandbox` viewer, who answers for whoever is asked). The socket
  takes `undo` (deletes the rows after the new sequence; every connection gets `game` and `log` again) and `inject` (a
  `SandboxEvent`) only in a sandbox. `GET /api/games/{id}/journal` exports a sandbox (`formatVersion`, `seed`,
  `playerCount`, `entries`) and `POST /api/sandbox/import` stores one as a new sandbox once the engine restores it.
- Regression journals: `src/test/resources/journals/*.json` are sandbox exports, and `RegressionJournalsTest` checks
  that each restores. A bug found in the sandbox becomes a test: export the journal, drop it there, load it with
  `RegressionJournals.engine(name)` and assert. A fuzzer finding becomes one with `./gradlew :backend:test --tests
  '*FuzzJournalExportTest' -DfuzzJournal=4:117` (`fuzz-4p-117.json`): the journal at the pause before the finding, the
  `input` that failed there (`player`, an `option` and/or an `event`) and what it has to do once fixed (`expect`:
  `runs`, `answers` or `restores`, see `Repro` in `fuzz/`), which `RegressionJournalsTest` checks per file. A fuzz
  journal that a `FORMAT_VERSION` change makes unloadable is deleted; the core test of its fix stays. A change that
  only adds types leaves the journals' entries as they are: their `formatVersion` goes up with it, and
  `RegressionJournalsTest` shows they still load. A journal
  can't check a fix that changes what an earlier event queued (a decision offered without an answer, a stale queued
  event): the journal records the followup, so a restore queues it again whatever the fix. Such a fix has its core
  test only.
- Flyway migrations in `src/main/resources/db/migration`. The tests (`BackendTest`) run against a Testcontainers
  Postgres with the `dev` profile; `LiveGameTest` drives real web socket clients and restarts the server mid-game, and
  `SandboxLiveTest` plays, undoes, exports and imports a sandbox. Those two and `GameSessionsTest` play `ScriptedTurns`
  beside an engine in memory, which asks for every answer, so they turn `frakkin.take-only-answers` off;
  `OnlyAnswersTest` plays with it on.

### Frontend

`frontend/` is a React 19 + TypeScript (strict) + Vite app, built by Gradle through node-gradle (its own Node 24, `npm
ci`): `./gradlew build` type checks, builds and tests it, and the backend's `bootJar` packs `frontend/dist` under
`static/`, with `AppRoutesController` forwarding the app's routes to `index.html`. React Router, TanStack Query for
REST (the lobby lists poll), Zustand for the game.

- The wire schema: `./gradlew :backend:exportSchema` writes `frontend/src/wire/schema.json` (`backend/wire/WireSchema`:
  every record with its components and type references, unions, enums, the socket messages, the `location` of a
  location's action, the `quorumCard` or `mutinyCard` a card's event plays, the `source` of an event (`@Source`) and
  the `sources` of an enum's constants (`GrantedBy`),
  the `colors` of `SkillCardType` (a hand lies by color, `table/skillCards.ts`), the `characters` of `CharacterAbility`
  (the sheet that prints each, its art) and its `miracles` (the tokens a miracle ability spends, `isMiracle`), the `types` and `skillSets` of `Character` (the skill set as its sheet prints
  it, for the strip beside a mat's portrait), the `attackTable` (`AttackTable` in `core`, read from the attack results' thresholds, for the cheat sheet), and a `role`
  for each int by its name (`ComponentRole` in `core`): `player`, `ship`, `scope` or `count`). `npm run gen` turns it into `src/wire/types.ts`. `WireSchemaTest` fails when the checked-in schema is stale, and `tsc` when the UI uses a field
  that went away. `GET /api/schema` serves the same JSON.
- `src/api/` (`http.ts` sends the `XSRF-TOKEN` cookie as `X-XSRF-TOKEN`, `queries.ts` the hooks), `src/live/`
  (`GameSocket` reconnects with backoff, `gameStore` holds the game, its token, the log, the last rejection and the
  `intent`: an answer to submit once the option just chosen asks its decision),
  `src/labels/` (all wording, in plain sentences: a humanized fallback, `overrides.ts` for names (an alternate sheet is
  named like its character, nowhere "alternate"), `templates.ts` a sentence per event and fact, the words of an answer
  on a button, of an event a player picked in a choice and of an event whose fields are hidden, in a small template
  language (optional parts, plurals, the words per constant, "the" in front of a location), `facts.ts` a scope's facts
  as sentences (a card drawn into a hand is "draws …", one lost to a discard pile "discards …", moves of a kind in a
  row and ships that moved alike are one sentence, the bookkeeping left out); they fill in as `Part`s so
  `art/Words.tsx` shows a named card, character or location on hover (`artOf`: `partArt`); an answer is an order to
  the player it names ("Choose the top option of Riots"); only names, never a card's printed text), `src/input/` (the input panel, see "Answers"; under it the `AnytimePanel`, the
  game's `anytime` answers as moves (`anytimeMoves`) in the answers' `Groups`, e.g. the Motive Cards whose requirement
  is met, shown only while a player the viewer acts for has one and sent as listed), `src/table/` (the board and the panels around it, see "The
  table"), `src/sandbox/` (undo, export, restart with the seed, setup by injecting a `SandboxEvent` that `SetupForm`
  builds from the schema, exactly the codec's JSON, the inspector),
  `src/pages/`.
- The browser holds no game rules: a picker only narrows to plausible answers, the engine judges them.
- Answers: `input/answers.ts` turns the pending input into `Move`s, one per concrete answer, shape
  (an example and a `Pick`) or option of a choice, and an option that asks a decision first into one per answer of that
  decision. `targetsOf(move)` lists what a move names (its kind, the location of a location action, locations, ships,
  other players, held cards, nested records too), `narrow` keeps the moves that name every pick of the `selection`
  store (`input/selection.ts`, cleared by each game message; "Back" and "Clear"), and `plan` says what to send: an
  answer is submitted, an option chosen, and an option with an answer of its decision chosen with the answer as the
  `intent`, which `TablePage` submits on the next game only when `intentDue` (a later game, the same player, a decision
  whose answers `cover` it), then keeps as `sent` until the game after it (the input panel is `aria-busy` while an
  intent is pending). An update with an `automatic` scope of its player drops it: the server took that only answer
  itself. `AnswerPanel` shows them: a choice's options in order, an option's decision as a section of
  its answers (the first one open), the answers grouped by kind (`groupMoves`; a group's moves differ in
  `varyingComponents`, worded by `moveLabels.ts`; the characters to choose at the start and after an execution
  (`SelectCharacterEvent`) lie by pool, side by side as large as their portraits: military leader, political leader,
  pilot, support, Cylon Leader), the Pass last (a `NoOpEvent`, or an answer that names none of what its group's others
  do, e.g. no interrupt card; its group then names the answer left by what told them apart) with its icon. Every other
  answer is a row of its own, as high as a card, its images in a slot as wide on every row so the words line up
  (`ArtSlot`). `input/answerArt.ts` picks them: what the answer names (`recordArt` with a `ValueArt` for the answers: a
  player as his character's portrait, a title as its insignia with the card as the preview, an ability as its
  character's portrait, a deck as its back, a Skill Card color as a back in it, a die result as the face, a
  resource as the board's icon (`resourceIconArt`), ships by their ids, no location: the board shows it), the event's `@Source`, what its kind does where its fields don't say
  (`KIND_ART`: the deck a draw takes from, the Skill Card back of the cards a random discard or the Destiny deck takes,
  the title a Title Card action is, the Destination back on each destination to choose, whose cards the choice shows
  once above its options, a move (`MoveEvent`, `LeaveBrigEvent`) and the head of the "Movement:" answers as the turn
  panel's Movement step: `stepIconArt` draws `StepIcon`'s markup as an image), the back of a shape's cards to pick (the
  Skill Card back for the cards to play into a skill check or to discard), and the miracle token last on every
  answer of a miracle ability. Neither the token nor a step's icon moves to the group's name (`staysOnEachAnswer`). A draw as a card per color that a click takes one more of, back to
  what every answer draws once it has the most an answer leaves it beside the other colors (what every answer draws
  is picked from the start, e.g. a skill set's one-color options in the Receive Skills step, and a color every answer
  draws as many of comes first, fixed, so a two-color option's colors are left to pick, held to their shared count; a
  color left no room is disabled in its place, one no answer has isn't shown), a shape as a picker (sorted cards, a counter, the strength against `minStrength`, the click order when ordered), the
  decision's constraints above them. A group whose answers are a product of what they differ in (`input/parts.ts`:
  more ways than its largest part has values, e.g. the "Engine Room"'s two cards, "Make a Deal"'s target, destination
  and drawer, a choice of every pair of damage tokens) is picked part by part instead (`PartsWidget`): a row of values
  per part (a list's elements, or numbered siblings such as `discardCard1`/`discardCard2`, as one pool in any order),
  each open while an answer still fits the picks, and the answer they name in full sent as the engine listed it; a
  pair in either order is one answer. `labels/pause.ts` words what the game waits for: the heading ("Laura Roslin
  decides", "Waiting for …") and a line from the decision and the input's `topic` ("Draw Skill Cards · Receive Skills
  step", "Crisis: Riots"), led by the card or ability the topic comes from when its words don't name it
  (`unnamedSource`: "Forward Thinker: Lee Adama may activate his location", "Executive Order: Move or Action"); a
  secret input says only that someone may play an interrupt card. The asked viewer's board
  gets a red edge.
- Reference (`table/reference/ReferenceDrawer.tsx`, the turn panel's "Reference" or the R key outside a field, which
  closes it too, a mat's portrait for its sheet):
  every card by deck with a search, the character sheets, the rulebook's sections and cheat sheets (the turn, a skill
  check, the Attack Table from the schema, the Crisis Card's icons, loyalty). `labels/rules.ts` indexes the in-scope
  articles of the combined rulebook and its Appendix by page (the PDFs ship as Vite assets) and gives each kind of input
  the article to read (`ruleFor`, the link beside the pause line). It names things and points to the article, it never
  repeats the rulebook's text; the section names and the cheat sheets are in our words.
- `src/art/`: the images of `materials-sorted/` (a folder per enum, a file per constant, a Skill Card as
  `TYPE_value`, a dial as `RESOURCE_value`; and for what no enum names, `Title/`, `Token/`, `Die/` and `Back/SKILL`).
  `images.ts` loads the folders with `import.meta.glob` (not the full board scan at the root), so Vite copies them
  into the build, and gives an `ArtRef` per component (`cardArt`, `skillCardArt`, `characterArt` (the standee, with
  the character sheet as the preview), `locationArt`, `shipArt`, `deckBackArt`, `titleArt`, `titleIconArt` (the
  insignia the mats' badges show too), `tokenArt`, `dieArt`, `skillColorBackArt` (drawn: the Skill decks print no
  back), ...) or `null` where there is none: the UI then shows the name. `refs.ts` finds what an event, a fact or an
  option names through the schema, for the log and the input. `Art.tsx`: `CardTile` (a face-up card is a `tile`, a
  face-down one a `tile back`, which the lobby e2e checks), `CardBack`, `ArtStrip`, `SvgArt`, and the hover preview
  (`ArtPreview`), which an image takes with it when it goes away under the pointer.
  `images.test.ts` checks that every constant has its image and every image names a constant.
- The table (`TablePage`): no bar over it, the board in the middle, as high as the screen (`.table-main` is a size
  container, the board's width follows its height), and a column on each side that scrolls on its own. On the left the
  game and the turn (`TurnPanel`, its tab names the game, with the connection beside it while it isn't open, the turn's
  number and the current character in a banner, red while he acts, otherwise amber with a red one of the acting
  character beside it, and the
  skill check under the turn's Crisis Card), the decision (`InputPanel`) and its prompts, and under them the log. The decision takes the height its answers need while the log keeps 15vh, and scrolls inside its body past that (under its heading and pause line, down to 12rem); the log takes the rest and scrolls inside its body, so the left column doesn't scroll as a whole, and the board's "your move" glow is clipped so the page never does. Neither has a title. The log (`table/log/LogPanel.tsx`, the story in `table/log/story.ts`) keeps every turn open (named with its current player), groups it by step (`StepChanged`, a step without a line has no header), and tells each scope as a line: a player's lines in a row stand under his token and name once, and an event about no player (no `playerNumber`) under none. A choice is told by the event it picked, under the player who picked it, and a pass by what it passed on ("doesn't move"); the passes on the cards every player is asked about, the scopes that only ask, and an event about no player that changed nothing are left out, and an event whose facts say everything (the steps, a skill check's mechanics, a roll, a result) is told by them, each under the player it is about. A line has a bolt icon in front of an only answer the server took ("automatic" on hover) and a tag with the card or ability it comes from when its words don't name it, and its facts under it, the ones that change the table first (`isStoryFact`; a ship destroyed, damaged or launched counts) and without the ones its words say already. A click on a line lights what it names on the board for a moment (`table/flash.ts`, `entryTargets`, `useFlash`). It follows the newest entry until the user scrolls up (`useFollowBottom`: only a scroll up leaves the end, since a scroll event arrives a frame late), and the arrow at its bottom right follows again, amber while new entries came below. What changed with the last game message plays once (`table/changes.ts`, `changesBetween` of `gameStore.previous` and the game, keyed by the version): a floating delta at a resource and the distance, and at the Admiral's nukes, the marker of the Jump Preparation or Pursuit track fading out of the space it left and in on its new one, a newly damaged location pulsing red, a ship gliding from its old place (FLIP on `data-ship`), the die tumbling, new cards sliding into a hand. A snapshot (a reconnect, an undo: `expectSnapshot`) leaves no previous game, so it animates nothing. On the
  right the sandbox's tools in a sandbox, above the players' mats (`PlayersPanel`, the title badges with the insignia
  cut out of the card (`table/players/icons/`, the card as their preview), the nukes beside the Admiral; right
  of the portrait a strip with a cell per card of the sheet's skill set, in its colors, split along the diagonal for
  "either", for everyone but a revealed Cylon, who draws 2 of any colors). Under 1100px the board goes across the width
  and the panels under it. The styles are the "table" section of `styles.css`: the board's palette, cut corners
  (`.cf`, `--chamfer`) and the printed-label look. A player is named by his character everywhere (`playerName`), "Player n"
  before he has one; the user in the seat shows only on his mat (`userName`, from the room's seats).
- The board (`src/table/board/`): `board.jpg` is the surface, and `BoardSvg` lays the components on its printed
  slots, in the image's pixels. `board.layout.json`, measured with the image, holds what is printed, and `layout.ts`
  reads it (a place for every `Location`, which a test checks, the title panels, the tracks, the resources, the deck
  rack, the launch tubes) and adds the free starfield (`SLOTS`). The sections' names (`labels/board.ts`) aren't printed:
  the board draws each where the picture once printed it (`LABELS`), a chip like the mats' badges in greys, on the top
  edge of a box that has one. The standees stand on the printed names of their
  locations, reaching a little into the pictures, and a damaged location gets a red "damaged" chip, like the Engine
  Room's "activated", at the bottom of the panel its name is printed in, under the name. The ships fly in the middle
  of their space areas, the Cylon ships first, a ship as large in every area of its board, alone or piled (the bow's
  strip is 8 o'clock, the stern's 2 o'clock; the ships leave the launch tubes free). The resources lie in a row
  of gauges where nothing is printed (`RESOURCES`): each its icon (`icons/`, flat one-colour SVGs) and a pip after it for each of its amount
  (up to 15, centred on the jump track's line), the red zone's pips a muted red that turns bright and glows while the amount is in it (`resources.tsx`),
  and hovering one shows its name and amount;
  markers sit on the tracks (the Boarding Party track isn't printed: `boarding-party.png`, red only at its end, lies
  over the hull while a centurion is on it, and its centurions stand large and glow red), the decks lie in the rack, which prints no names but rings each Skill Card deck in its
  color, with their counts and their top discard over them, the civilian ships lie at the end of the ship reserves, and
  the destinations tuck under the objective, each showing its distance. The Crisis Cards kept in play lie by the jump
  track. The board leaves out the card being resolved, its skill check and the die (the turn panel shows them), the
  damage token piles (only the tokens in play show) and the nukes (the Admiral's mat). Hovering a location shows its
  card, a standee its character sheet with its player's mat beside it (`PlayerMatOf`, the preview's `extra`), and a
  pile of civilian ships shows what each costs
  when destroyed to whoever knows it (a sandbox, the players who looked at it). The board and the mats answer the
  pending input (`src/input/boardAnswers.ts`, `AnsweringContext`): `offerOf(moves, picks)` lights what the moves left
  name, the locations, ships (a ring) and standees of one kind of move at a time (the only kind left, or the only one
  naming any; the Action step lights none), the "Act" chip of each location action (a click picks its kind), and the
  asked player's cards that start a move (`Playable` on his mat; a sandbox shows the asked player's own cards face up).
  A player target lights his standee and his mat. A click picks the target: the only move left is sent, a shape opens
  its picker in the panel, and several stay in the panel and in `AnswerPopover` next to the click, e.g. a viper's
  activations or a card's targets. A lit target's `<title>` says what clicking it does. A group's title in the panel
  picks its kind, so the board lights its targets.
- Hover (`src/table/reveal.tsx`): a pile shows what it holds in one fixed popover (`RevealLayer`), its cards as large as
  their images (no preview of their own) in rows of up to 860px, and scrolls when they don't fit the window. The
  preview of a single image (`ArtPreview`) shows it as large as it is too, scaled down only by a smaller window; a card
  back has none, so it never covers its pile's popover. Neither, nor the reference's grids, puts a name under an image
  (it stays the image's `alt`); a pile keeps a label that says more, e.g. who played the card. A face-up discard
  pile shows to everyone. A sandbox looks like a live table, every hand and deck face down, and shows on hover what the
  players can't see (`hidden` sections: hands, Loyalty and other player cards, draw piles, face-down discards, a skill
  check's cards). The cards played before a skill check lie face up there too once revealed (`playedBeforeRevealed`).
- The website: `GamesPage` (the lobby: my sandboxes and games, open lobbies, games to watch, hosting), `RoomPage`
  (seats and observers, join, leave, start), `SettingsPage`. A 401 goes to the login page. The lobby's pages and a
  game's room have the top bar (`TopNav`), the table doesn't. A game opens in a tab of its own: the links to it, and
  hosting, joining, a new or imported sandbox through `openGameTab` (`src/pages/gameTab.ts`), which opens the tab on the
  click, while the browser allows it, and points it at the game when the request answers. A sandbox's "Restart with
  this seed" stays in its tab.
- e2e (`frontend/e2e/`): each spec follows a game into its tab (`tabs.ts`). `sandbox.spec.ts` plays a seeded sandbox through a turn (reading the stacked Crisis Card
  from the Crisis deck's hover, Starbuck's "Secret Destiny" letting it resolve; the seed seats the first player),
  undoes, exports and imports;
  `lobby.spec.ts` has five users host, join, observe, leave and start a game, checks that nobody sees another
  player's cards and that an observer gets no input, and races two users for the last seat. `playthrough.spec.ts`
  plays a seeded four-player sandbox (`PLAYTHROUGH_SEED`, 7 by default) to an ending through the answers alone, picked
  at random from a seeded generator (the options, the answers by kind, the counters, the parts, the pickers), so an
  input without answers fails it. It takes minutes.
- Commands: `npm run dev` (Vite on 5173, proxying `/api`, `/ws`, `/oauth2` to 8080; the `dev` profile allows its
  origin), `npm test` (Vitest), `npm run gen`, `npm run e2e` (Playwright, against the running app with the `dev`
  profile, `E2E_BASE_URL` to point elsewhere).

### Character Abilities

`CharacterAbility` lists the three abilities of each sheet (`CharacterAbility.of(character)`), with its `AbilityKind`
(`ACTION`, `MOVEMENT`, `TRIGGER`, `PASSIVE`, `NEGATIVE`) and the miracle tokens a use spends (`isMiracle()`: 1, 3 for
"Broadcast", rule 4.3.1.1). Every ability is built; below, grouped by where it
hooks in. An ability that answers something is offered only to a player who can use it, so a game without its
character pauses where it did.

- `PlayerView.hasAbility(ability)`: the character prints it and it is in effect. A revealed Cylon's are ignored (rule
  6.1.2.2), a Cylon Leader's always apply (rule 6.2.1.2). E.g. "Reckless" (`handLimit()`) and "Starts on the Bench".
- `CharacterAbilities` (`event/character/`): `canUseMiracle` (the ability, its tokens, and no "The Farm" in play for
  a human), `canUseOncePerTurn`, and `canAffectSkillCheck` (false for a Super Crisis Card's check, rule 6.1.2.5).
- `PlayerOps.useAbility(ability)` records `PlayerFact.AbilityUsed` (public, for the log and the turn's
  `usedAbilities()`) and a `MiracleTokenLost` per token a miracle spends.
- Miracle tokens are a count (`PlayerView.miracleTokens()`, `maxMiracleTokens()`: 1, 3 for Gaius Baltar (alt.)).
  `PlayerOps.gainMiracleToken` gains one only when `canGainMiracleToken()` (below the maximum, not a revealed Cylon,
  rules 4.3.1 to 4.3.3), and `loseMiracleTokens()` discards them all.
- Setup and skill draws: "Disreputable" (`SelectCharacterEventProcessor` draws the Mutiny Card, at setup and for a
  new character), "Coward" (`CreateLoyaltyDeckEventProcessor`), "Sleeper Agent" (`SleeperAgentEvent`), "Industrious",
  "Family Commitments", "Grieving" and "Starts on the Bench" (`ReceiveSkillsStepEventProcessor`, no decision when he
  draws none), "Reckless", "Stranded" and "Nothing But Trouble" (its skill set).
- The "Action:" abilities follow the Mutiny Cards' pattern: a `CharacterActionEvent` (an `ActionEvent`) per ability in
  `event/action/character/`, named after it, with its choices on it. `CharacterActionEventProcessor`'s final
  `isValid()` asks `CharacterActions.canUse` (the ability in effect, its miracle tokens, and the processor's static
  `canUse`), then `isValidUse()`; its final `process()` records the use, then `resolve()`. `CharacterActions` has an
  exhaustive switch over `CharacterAbility` (`canUse`, `eventClass`), and `canUseAny` feeds
  `Actions.canTakeOtherAction`. A Cylon Leader gets them beside his location's action (rule 6.2.1.2), a revealed
  Cylon doesn't (rule 6.1.2.2). `CharacterActionEventsTest` checks one event per `ACTION` ability.
- A choice that depends on what the ability reveals comes after it as concrete options naming the player
  (`SkilledPoliticianChoiceEvent`, `AbusePowerChoiceEvent`, `QuarantineChoiceEvent`, `TreatmentGiveEvent`) or a
  decision (`ReorderTopQuorumCardsEvent`). "Skilled Politician" holds the chosen Quorum Card (`PlayerPile.QUORUM`) and
  plays it through a decision on its event, as "Popular Influence" does; "Abuse Power" holds the chosen Mutiny Card to
  play it, which sends nobody to the "Brig" (`PlayerOps.drawMutinyCardsToChoose`, `holdMutinyCard`).
- Actions at another location ("Organized", "Resolute", `RemoteActions`): `GameFact.RemoteActionAllowed(player,
  location)` lets that player take the location's action from wherever he is, through the location's own decision
  (`TurnView.remoteActionLocation`, which `LocationActionProcessor` honours: only that location then), and
  `EndRemoteActionEvent` ends it. `RemoteActions.locations` lists those he could act at: undamaged, with an action, not
  the "Brig".
- "Blind Jump" queues `JumpingTheFleetEvent(true)`: the Admiral draws 1 Destination Card and the fleet goes there
  (`DrawDestinationCardsEvent(blindJump)`). "Expert Pilot" records its use when Starbuck starts her turn piloting;
  the Action step then adds `GrantEvent(ACTION, EXPERT_PILOT)`. "Meticulous" turns Aaron Doral's return action into a
  choice of `MeticulousEvent`s (the "Resurrection Ship", or another Cylon location and `GrantEvent(ACTION,
  METICULOUS)`).
- The "Movement:" abilities ("Intimate", "Aggressive Tactics", "Glimpse the Face of God") are
  `CharacterMovementEvent`s (`MovementAbilityEvent`s) with `CharacterMovementEventProcessor` and `CharacterMovements`,
  as the actions have; `Actions.offerMovement` asks `CharacterMovements.canUseAny`, Cylon Leaders included (rule
  6.1.3.2). "Aggressive Tactics" grants two actions (`GrantEvent(ACTION, AGGRESSIVE_TACTICS)`), and "Glimpse the Face
  of God" draws from the Destiny deck into the hand (`PlayerOps.drawFromDestinyDeck`), then asks for
  `GlimpseTheFaceOfGodPlaceEvent` (`PlayerOps.placeOnDestinyDeck`, the first card on top).
- The die-roll abilities are answers in the interrupt windows (`RollAbilities`, `UseRollAbilityEvent`, see "Interrupt
  Cards"): "ECO Officer" (Helo on his turn, once a turn) and "FTL Operator" (Gaeta, once on each "FTL Control" roll)
  reroll in a window after the roll; "Longshot" (Anders on his turn, a miracle) chooses the result and "Hotshot" (Kat
  in her own Action step) discards a card for its strength +2, in a window before it. "Longshot" sends a "Strategic
  Planning" played with it back to the hand; "Hotshot"'s face takes the cards; neither face can be rerolled.
- Moving (`CharacterMoves`): "Clouded" refuses a destination with another character for every move Leoben chooses
  (`MoveEvent`, the "Human Fleet" infiltration, `LeaveBrigEvent`, the return to the "Resurrection Ship"); "Heretic" makes D'Anna pay a Super Crisis Card to
  leave the "Resurrection Ship" (`HereticDiscardEvent` options after the move, `PlayerOps.discardSuperCrisisCard`), and
  `Actions.canMove` is false while she has none.
- The ends of steps: `EndOfMovementStepEvent` offers "Politically Adroit" (`PoliticallyAdroitEvent`: a card to another
  human there, then a `DRAW_EXACTLY_2` decision) and applies "Kleptomania" (a forced discard of 2, or
  `GameFact.SentToBrigAtEndOfTurn`, which the Discard step turns into a `KleptomaniaBrigEvent` after the discards).
  `EndOfActionStepEvent` applies "Stim Junkie": `StartTurnEvent` notes Kat's start (`GameFact.StartLocationNoted`,
  `TurnView.startLocation()`), and she goes to "Sickbay" when she ends her Action step there.
- The Crisis Card abilities, none of which touches a Super Crisis Card (`SkillCheckType.onCrisisCard()`):
  - Drawing: "Religious Visions" (the Crisis step draws 2, `ReligiousVisionsEvent`, nothing to choose between two
    copies), "Delusional Intuition" (`DelusionalIntuitionEvent`, a `DRAW_EXACTLY_1_OF_ANY_COLOR` decision, also after the Gas
    Cloud's draw), "Secret
    Destiny" (`SecretDestinyEvent`: `GameOps.discardRevealedCrisisCard`, then a new card is drawn and revealed), all
    through `CrisisCardReveals`. "Inspirational Leader" counts 1 strength cards positive on a Crisis Card's check
    while William Adama is the current and the acting player (`TotalSkillCheckStrengthEventProcessor`).
  - Choosing: "Amoral", "Choose a Different Path" and "Deceitful" in `CrisisCardFollowups` (see "Crisis Cards"),
    "Moral Compass" after a section choice (`MoralCompassEvent`, only on another player's choice).
  - "Mysterious Intuition" (`MysteriousIntuitionEvent`, a pass, a fail or nothing) before a Crisis Card's check;
    "Modifications" (`ModificationsEvent`, one per `CylonActivation`) instead of the card's icon at the start of an
    Activate Cylon Ships step, also one without a Crisis Card (`offerWithoutCard`); "Recon" (`ReconEvent`, then `LookAtTopCardEvent`) last in the Discard step.
  - Where the alternative is to go on, the option to go on is the plain event that follows (e.g.
    `one(SecretDestinyEvent, ResolveCrisisCardEvent)`): it names nobody, so the chooser is the ability's player.
- The skill check abilities, none of which affects a Super Crisis Card's check (`canAffectSkillCheck`),
  each with an `offer(game)` that is `NONE` when nobody can use it (see "Skill Check Sequence" for where):
  - Cards into a check: `PlayCardsIntoSkillCheckEventProcessor.maxCards` and `canContribute` hold the limits, "Vanity",
    "Calculating" and "Misguided" included, and "Impulsive" (never exactly 1 outside the "Brig"). Only a player who may
    put a card in is asked (`eachPlayerFromTheLeft` with a filter). "Logic-Bound" then asks Simon O'Neill which of his
    cards goes face up (`LogicBoundEvent`, a `CardsShown`).
  - "Fast Learner" (`FastLearnerEvent`: the top 3 of a deck into Dee's hand, then `FastLearnerIntoCheckEvent` or not),
    "Blind Devotion" (`BlindDevotionEvent`, `SkillCheckFact.StrengthZeroed`: the color counts 0 in
    `SkillCheck.cardValue`), "Quick Fix" (`QuickFixEvent`: Cally discards a revealed card on her turn), "Human Delusion"
    (a decision on `HumanDelusionEvent`: cards from her hand face up into their piles, `SkillCheckOps.addRevealedCards`,
    for the strength only, their abilities don't resolve) and "Command Authority" (`CommandAuthorityEvent`:
    `SkillCheckOps.takeRevealedCard` for each revealed card).
  - Location checks: "Insubordinate" (-3 when Starbuck is chosen) and "Cylon Hatred" (`CylonHatredEvent`, -3) at the
    "Admiral's Quarters", "Friends in Low Places" (`FriendsInLowPlacesEvent`, ±2) at the "Brig", through
    `SkillCheckFact.DifficultyAdjusted`, and "Intolerant" (`IntolerantEvent` beside the `SendToBrigEvent` of an
    "Admiral's Quarters" pass of 10 or more, `SkillCheckResultFollowups`).
- The other triggers, queued only in games with their character, each offered only when it can be used:
  - Right after any event that caused it: an op notes the trigger (`PlayerFact.AbilityTriggered`,
    `TurnView.triggeredAbilities()`), and the engine's `AbilityTriggers.check` (after `WinChecks`) queues its offer,
    which clears it (`AbilityTriggerCleared`). It queues one offer at a time: the check after that offer queues the
    next trigger, so no offer is queued twice. `AbstractOps.loseResource` notes "Memento" on a population loss
    (`OfferMementoEvent` offers `MementoEvent`: 3 Piloting cards drawn, one discarded) and "Emotionally Fragile" when
    morale ends at 2 or less (`EmotionallyFragileEvent` queues `ExecuteEvent(n, withoutMoraleLoss = true)`).
  - "For Love": the discards a player is made to do go through `ForcedDiscards` (`DiscardKind`: chosen, random, the
    whole hand), which in a game with Sharon "Athena" Agathon queues a `ForcedDiscardEvent` that asks her
    (`ForLoveEvent`) before the discard happens. `DeceitfulDiscards`, the reveal powers and the Final Five discards use
    it; a cost a player chooses, the hand limit and a Super Crisis Card don't.
  - Where they happen: "Escort" (`EscortEventProcessor.offer` in `DestroyCivilianShipEvent`, unless `escortOffered`),
    "Necessary Steps" (`NecessaryStepsEventProcessor.drawFor` in `DrawMutinyCardEvent` and the "Press Room";
    `DrawMutinyCardEventProcessor.afterDrawing` then applies the "Brig" rules), "Dutiful" (`OfferDutifulEvent` after
    "Command", "Communications" and "Weapons Control", wherever he activated them from, a remote action again from
    elsewhere, never inside "Organized"; `DutifulAgainEvent` offers the action once the discard is done, which may
    have sent him to the "Brig"), "Alert Viper Pilot" (after an unmanned launch from the "Reserves", and when
    "Launch Reserves" places its vipers, before it activates them, `GrantEvent(ACTION, ALERT_VIPER_PILOT)`), "Forward Thinker"
    (`OfferForwardThinkerEvent` after an "Executive Order", a decision on any `LocationEvent`), "Maintenance Engineer"
    (after "Repair", `GrantEvent(ACTION, MAINTENANCE_ENGINEER)`), "Raptor Pilot" (`RaptorPilotEvent` instead of "Launch
    Scout"'s look), "Adaptable" (`OfferAdaptableEvent` after any Quorum Card), "No One Gets Left Behind" (in the jump,
    after the Cylon ships move), "Manipulative" (first in `StartTurnEvent`, `GameFact.TitleBorrowed`, given back by
    `EndTurnEvent`) and "Efficient" (Dee's "Communications" looks at every civilian ship).
- The penalties (`CharacterPenalties`), each adding nothing to a game without its character: "Lies and Secrets" (Cavil
  discards his hand after any "You Are a Cylon" reveal, by action or execution), "Moral Dilemma" (2 cards after each
  Mutiny Card Lee Adama gets, in `DrawMutinyCardEventProcessor.afterDrawing` and the "Press Room"), "Alcoholic" (first
  in `StartTurnEvent`), "Forced to Eject" (`destroyedInsteadOfDamaged`, where a fighter in space is damaged) and
  "Headstrong" (`ForcedDiscards` makes Apollo's chosen discards random, and so does his hand limit).
- The bans (`CharacterBans`): "Emotionally Attached", "Bent on Revenge" and "Convicted Criminal" in
  `LocationEvent.canActIgnoringDamage`, "Specialized" in `SkillCardPlays.canPlay` and `SkillCardEventProcessor`,
  "Spiritual Crisis" in `RevealCylonEventProcessor.canReveal`. "Nothing But Trouble"'s note bans nothing that exists:
  no Treachery Card has an action, their text resolves in skill checks (`CharacterBansTest` pins it).
- The costs (`ActionCosts`): "Terminal Illness" (2 cards before any location), "Conflicted Loyalties" (1 for the
  "Cylon Fleet") and "Reluctant" (1 more for a Skill Card's "Action:"). The player pays first: the processor records
  `GameFact.ActionCostPaid(n)` and queues the discard and the same event again, which then acts and settles it
  (`ActionCostPaid(null)`), or does nothing when the discard took away what it needed. The location cost is a
  `discardExactly(n)` decision, "Reluctant"'s a choice of the other cards, so the card played stays in the hand. An
  action that can't be paid isn't offered.

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
  test-only events with their processors and `TestOps`, `GameSnapshot`, `ScriptedScenario`, `ScriptedTurns`,
  `TestEngines` (starts an engine with any first event) and the random playouts (`fuzz/`, see "Random playouts").
  `ArchitectureTest` leaves them out, like the tests
- Random playouts (`fuzz/`): `Playout.play(players, seed)` plays a seeded game with random legal
  answers until it ends, a finding stops it or it runs past a cap, and restores it on the way (the twins have to stay
  equal). `AnswerFinder` answers from the game's `LegalAnswers` (see "Legal answers"): a random class among them, then
  a random answer of it, a shape filled at random and judged again (its example when no filling fits). Empty answers
  are a `NO_ANSWER`, or a `DEAD_OPTION` for a choice, which looks ahead at the decision an option leads to. At 1 in 20
  decisions a `Probe` first submits a hostile candidate (`Candidates.hostile`, from the wide domains of `Domains.wide`:
  any card of the decks, players 0 to n + 1, unknown ship ids, nulls), which the engine has to reject leaving the game
  as it was (`Twins`), or take, and then the answers have to cover it. At 1 in 10 answers `Mutations` sets one
  component to each of its wide values: an input the game takes that the answers don't cover is a `HINT_MISSING`. A
  `Finding` (`CRASH`, `INPUT_CRASH`, `NO_ANSWER`, `DEAD_OPTION`, `HINT_MISSING`, `RESTORE`, `NO_END`, `BAD_ENDING`,
  `REJECTED_BUT_CHANGED`, `FUZZER`) has
  an ID from a signature without line numbers, and its `Pause` (where, the pending input, the inputs before it, and
  with `Options.withRepro` the `Repro`: the journal there and the input that failed). `Endings.classify` reads the
  rule's ending from the final state. `FuzzRun` (`./gradlew :core:fuzz`, `-Pgames`, `-Pplayers`, `-Pseed`, ...) plays
  the seeds on a thread pool and `FuzzReport` rewrites the block of `fuzzer-report.md` between its markers (statuses
  from the hand-written "Fixes" and "Questions"), with a trace per finding in `core/build/fuzz`. `AnswerFinderTest`,
  `CandidatesTest`, `EndingsTest`, `PlayoutTest`, `ProbeTest` and `FuzzReportTest` cover it without failing on what
  the engine does wrong
- `GameSnapshot` (`engine/journal/`) - turns a `Game` into comparable plain values. Replay tests compare a live game
  with its replayed twin. `ScriptedScenario.play(journal)` records every kind of fact, which `GameReplayerTest`
  checks. One of its scopes has no event, so only `GameReplayer` rebuilds it, not `GameEngine.restore`
- `ScriptedTurns` is `TurnLoopTest`'s seeded game as a list of inputs, each computed from the game when it's its turn,
  for tests that drive a game from outside (the codec, the sessions, the web socket). Starbuck lets each revealed
  Crisis Card resolve ("Secret Destiny") and Laura Roslin picks from her 2 ("Religious Visions"), so 5 cards are
  stacked
- Op tests also assert the logged facts (`journal.facts()`), and `FactApplierTest` covers each fact
- `GameEngineTest` plays a script of real events through `submit` and `choose`, started by the test-only `ScriptEvent`
  (it calls `ops.startGame()` and queues the script). The registry finds test processors such as
  `ScriptEventProcessor` too, because it scans the whole classpath. At every pause, `restore` has to give the same
  `GameSnapshot` and statuses. The test-only `FailingEvent` changes the game and then throws, to test rollback, and the
  test-only `AnytimeTestEvent` (spends its player's miracle token) runs ahead of decisions, choices and secret inputs.
  Engine tests use a seeded `Randomness`, not `FakeDeck`, because restore rebuilds the real decks
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
  instruction of a card, "Fulfiller of Prophecy" returning to the step, and "Secret Destiny". `CrisisAbilitiesTest`
  plays the other abilities that act on Crisis Cards, and `SkillCheckAbilitiesTest` the ones that act in a skill check,
  each from the location action to the end of the check. `CharacterTriggersTest` plays the triggers that answer
  something wherever it happens ("Memento", "For Love", "Necessary Steps", "Adaptable", "Manipulative"), and
  `CharacterPenaltiesTest` the penalties, bans and costs. `CrisisCardResolutionTest` runs every section
  of every card through the real processors with a minimal engine, so a section can't end in an event without a processor
- `InterruptWindowTest` plays interrupt windows through `submit`, restoring at every pause: the secret status, a pass,
  a tiebreak, an "Evasive Maneuvers" reroll. The test-only `GiveSkillCardEvent` and `PlaceRaiderEvent` set up the
  hands and the board, which a seeded game can't choose
- `RollAbilitiesTest` plays the die-roll abilities the same way: a reroll, a card and an ability from one player in a
  secret window, "Longshot", "FTL Operator" on a scripted "FTL Control" roll, and "Hotshot" in a real Action step
- `SkillCardActionsTest` plays Skill Cards for their "Action:" and "Movement:" through `submit` and `choose`,
  restoring at every pause. `GiveSkillCardEvent` deals the cards, and a script starts at the step that offers them
- `QuorumCardActionsTest` plays Quorum Cards the same way: the sandbox's `GiveQuorumCardToPresidentEvent` deals them,
  and `QuorumCardEventsTest` checks that each card has exactly one event. `MutinyCardActionsTest` and
  `MutinyCardEventsTest` do the same for Mutiny Cards, dealt with `GiveMutinyCardToPlayerEvent`
- `CharacterActionsTest` uses "Action:" abilities of character sheets through `submit` and `choose` ("Organized"'s
  three locations, "Skilled Politician", "Abuse Power"), stacking decks with `StackDeckEvent`, and
  `CharacterActionEventsTest` checks that each `ACTION` ability has exactly one event. `CharacterMovementTest` does the
  same for the "Movement:" abilities and the ones that act on moving or at the end of a step, in 4-player games (a
  Cylon Leader needs more than 3 players), and `CharacterMovementEventsTest` checks one event per `MOVEMENT` ability
- `SuperCrisisCardTest` has a revealed Cylon play Super Crisis Cards at "Caprica" through `submit` and `choose`, dealt
  with `GiveSuperCrisisCardToPlayerEvent`, and `SuperCrisisCardResolutionTest` runs every section and result of every
  card through the real processors, like `CrisisCardResolutionTest`
- The other engine tests play one component each through `submit` and `choose`, restoring
  at every pause: `TitleCardActionsTest` (the nuke, the CAG's two actions), `CylonRevealTest` (a power, the discards
  before it, a reveal on another player's turn, the execution's choice), `CylonLeaderTest` (the return to the
  "Resurrection Ship"), `FinalFiveTest`, `PegasusDestructionTest` and `SevenPlayerSetupTest` (a 7 player start with
  its Cylon Leader)
- `MotiveCardRevealTest` has a Cylon Leader reveal a Motive Card through `submit` while another player decides and while
  he chooses, restoring at every pause. The test-only `DrawMotiveCardsEvent` deals the cards a `StackDeckEvent` put on
  top of the Motive deck
- `PersonalGoalTest` reveals a Personal Goal through `submit`, restoring at every pause: "Self Destruction" with the
  new Loyalty Card, and "Selfish" with its discards. `CreateLoyaltyDeckEvent` builds the decks, the sandbox's
  `GiveLoyaltyCardToPlayerEvent` deals the card
- `CharacterSetupTest` starts a seeded game with Tom Zarek (alt.) and Samuel T. Anders through `submit` and `choose`,
  restoring at every pause: the Mutiny Card "Disreputable" starts with, and the Receive Skills step "Starts on the
  Bench" skips
- `JumpAndKobolTest` plays four jumps and the Kobol end game through `submit` and `choose`, restoring at every pause: the
  Sleeper Agent phase, a human win, and a Personal Goal that turns it into a Cylon win. The test-only `StackDeckEvent`
  stacks the Destination and Loyalty decks, which a seeded game can't choose
- Tests follow the pattern: set up game state, execute the event, assert state changes and followups. Use real game ops
  in processor tests, not mocks, since processors may read their own changes
