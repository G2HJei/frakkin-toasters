# Code map

`core`'s packages, under `xyz.zlatanov.frakkintoasters`. Each links to the topic file that explains it.

- root: `EventProcessor` (base class of all processors), the attacks and the viper activation.
- `event/`: event records and their processors, grouped by area: `action`, `board`, `character`, `crisis`, `deck`,
  `endgame`, `interrupt`, `jump`, `location/{galactica,pegasus,colonialone,cylon}`, `loyalty`, `officials`, `player`,
  `sandbox`, `ship`, `skillcheck`, `skillcheck/ability`, `skillcheck/result`, `turn`. Also `placeholder/`
  (`PlayerDecisionEvent`) and `constraint/` (`EventConstraint`, `ConstrainedEvent`). See [events.md](events.md).
  - `location/`: `LocationActionProcessor`, the base of the location action processors.
  - `action/`: `Actions` (what a player can do now), the Skill, Quorum and Mutiny Card plays, the character actions and
    movements, the grants; an event per Skill Card "Action:"/"Movement:" (`action/skillcard/`), per Quorum Card
    (`action/quorum/`), per Mutiny Card (`action/mutiny/`), per sheet "Action:"/"Movement:" (`action/character/`), and
    the Title Card actions (`action/title/`). See [actions.md](actions.md) and
    [quorum-and-mutiny-cards.md](quorum-and-mutiny-cards.md).
  - `character/`: `CharacterAbilities`, the bans, costs and penalties, the other ability events
    ([character-abilities.md](character-abilities.md)).
  - `loyalty/`: the Cylon reveal, the Final Five, Personal Goals, Motive Cards
    ([cylons-and-loyalty.md](cylons-and-loyalty.md)).
- `operation/`: the game ops, the only code that decides what changes. `GameOps` (entry point) hands out `PlayerOps`,
  `ShipOps`, `GalacticaOps`, `PegasusOps`, `CylonFleetOps`, `SkillCheckOps`, `LoyaltyDeckOps`, `TitleOps`,
  `InterruptOps`. They extend `AbstractOps` (holds the `Game` and a `FactRecorder`; offers `record`, `draw`,
  `shuffle`). Ops record facts, they never write to components ([journal.md](journal.md)).
- `engine/`: `GameEngine`, `Agenda` (stack of pending followups), `AgendaReplayer`, `ProcessorRegistry` (event class
  to processor) and `EventCatalog` (every event type), both scanned once by `ClasspathScanner`; `PendingInput`,
  `EngineStatus`, `ScopeActor` ([engine.md](engine.md)).
- `engine/answer/`: `LegalAnswers`, `ShapedAnswer`, `Pick`, and the search: `AnswerClasses`, `Domains`, `Shapes`,
  `AnswerSearch` ([engine.md](engine.md#legal-answers)).
- `engine/journal/`: `Journal`, `JournalEntry`, `FactRecorder`, `FactApplier`, `ShipZones`, `GameReplayer`, and `fact/`
  with the `Fact` records ([journal.md](journal.md)).
- `projection/`: what a `Viewer` may see, as plain records the backend turns into JSON
  ([projections.md](projections.md)).
- `state/`: the game state model. `Game` (top level), `Turn` (see [turn-loop.md](turn-loop.md)), `Player` (`turns()` counts his own turns),
  `Team` (the winner in `GameView.outcome()`), `Die` (d8), `DieRoll`, `Randomness`, `RemovedComponents`, `PlayerPile`,
  the `@Mutates` marker.
  - `state/interrupt/`: `InterruptWindow` (sealed records, the cards each takes), `PlayedInterruptCard`.
  - `state/view/`: read-only view interfaces (`GameView`, `TurnView`, `DieRollView`, `PlayerView`, `BoardsHolderView`,
    `BattlestarBoardView` (damaged locations), `DecksHolderView` (Skill Cards and civilian ships left), `SkillCheckView`, ...) implemented by the state classes. Processors read
    state only through these.
  - `state/board/`: `Board` (tracks characters by location) -> `BattlestarBoard` (damage) ->
    `GalacticaBoard`/`PegasusBoard`; `CylonFleetBoard`, `SpaceLocationsBoard`, `BoardsHolder` (all boards). The
    `Location` enum has the 35 board spaces in scope, Colonial One as in Daybreak.
  - `state/ship/`: `Ship` with `Viper`, `ViperMarkVII`, `Raptor`, `AssaultRaptor`, `Basestar`, `Raider`,
    `HeavyRaider`, `Centurion`, `CivilianShip`. `CylonShips` manages the Cylon ship supply. Ships have unique int ids.
  - `state/deck/`: generic `Deck<T>` (draw/discard/shuffle); `DecksHolder` holds all decks and routes discards.
  - `state/skill/`: `SkillCard`, `SkillCardType`, `SkillCardColor` (6 colors), `SkillCheck` (the active check and its
    piles), `SkillCheckType`, `PlayedCard`.
  - `state/card/`: `LoyaltyCard`, `QuorumCard`, `MutinyCard`, `ObjectiveCard`, `DestinationCard`, `MotiveCard`,
    `QuorumCardInPlay`.
  - `state/crisis/`: `CrisisCard` (with its printed `CrisisChooser` and `CylonActivation`), `SuperCrisisCard`.
  - `state/damage/`: `GalacticaDamage`, `PegasusDamage`, `BasestarDamage`.
  - `state/track/`: `BoardingParty`, `JumpPreparation`, `Pursuit`.
  - `state/character/`: `Character` (33 characters), `CharacterType`, `CharacterAbility` (the 99 abilities, with
    character, `AbilityKind` and miracle tokens).
  - `state/util/`: `AllCardsProvider` (the full decks in canonical order; the Skill decks follow the strengths of
    Appendix A2.3.8, `AllCardsProviderTest`).
  - `state/exception/`: `InvalidActionException` (an event failed validation) and `FrakCallTheAdmiralException`
    (should never happen: a programming error).
