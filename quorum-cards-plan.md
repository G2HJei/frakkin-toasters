# Quorum Cards plan

Status (2026-11-21): phases 1 to 5 are done.

Implements the 23 Quorum Card types, and everything around them that `unimplemented-components.md` lists as missing:

- The President plays them as an action (rule 8.4), and the Quorum Chamber plays them. Today that is bug 1: the
  Quorum Chamber's play option rolls back every answer.
- The President's title action, "draw a Quorum Card". It can't be reached today.
- The deck of 29 cards (bug 6) and the hand limit of 10 (bug 7).
- The cards that stay in play: the four "Assign" roles, "Probation" and "Accept Prophecy". They have their effects,
  and they are discarded on execution and at the end of an Infiltration.
- "Popular Influence", the last Skill Card. It waits for this plan (phase 10 of `skill-cards-plan.md`).
- The building blocks the cards need, listed in the review: a fact for a card that leaves the game, `MakeCagEvent`,
  checks in `MakeAdmiralEvent`, destroying one heavy raider or a chosen centurion, a destination choice that isn't fixed
  to the Admiral and 2 cards, and the morale lost to an execution.

Every phase ends with the tests green. The plan is done when:

- each card can be played in a game that `GameEngine.start` began, through `submit` and `choose`, with `restore` giving
  the same game at every pause;
- each card in play changes the game at the moment it names;
- "Popular Influence" can be played.

The card effects below are paraphrases. The images in `materials-sorted/QuorumCard/`, the President Title Card, "Popular
Influence" and the rulebook in `materials-sorted/bsg-rules/` were read for this plan: rules 4.4, 6.1.1, 6.2.3.3,
6.2.3.5, 7.1 and 8.4, and Appendix A2.3.13 and A4.6.1. Where they say nothing, the plan makes a choice (see "Open
questions").

## The cards

| Card | Copies | Effect | Event | Phase |
|---|---|---|---|---|
| Enact Production Quotas | 1 | +1 food, -1 morale | `EnactProductionQuotasEvent` | 1 |
| Establish Dogsville | 1 | +1 population, -1 morale | `EstablishDogsvilleEvent` | 1 |
| Food Rationing | 2 | Roll. On 6+, +1 food and the card leaves the game, otherwise discard it | `FoodRationingEvent` | 1 |
| Inspirational Speech | 4 | The same, with +1 morale | `InspirationalSpeechEvent` | 1 |
| Arrest Order | 2 | Send a character to the "Brig" | `ArrestOrderEvent(target)` | 2 |
| Execute Prisoner | 1 | Execute a character in the "Brig" | `ExecutePrisonerEvent(target)` | 2 |
| Presidential Pardon | 1 | Move another character from the "Brig" to another Galactica location | `PresidentialPardonEvent(target, destination)` | 2 |
| Release Cylon Mugshots | 1 | Look at a random Loyalty Card of another player, then roll: on 3 or less, -1 morale | `ReleaseCylonMugshotsEvent(target)` | 2 |
| Encourage Mutiny | 1 | Another player who isn't Admiral rolls: on 3+ he becomes Admiral, otherwise -1 morale | `EncourageMutinyEvent(target)` | 2 |
| Presidential Order | 1 | Give a player the Admiral or the CAG title | `PresidentialOrderEvent(target, title)` | 2 |
| Eulogy | 1 | +1 morale, if morale was ever lost to an execution | `EulogyEvent` | 2 |
| Authorization of Brutal Force | 2 (unverified) | Destroy 3 raiders, 1 heavy raider or 1 centurion, then roll: on 2 or less, -1 population | `AuthorizationOfBrutalForceEvent(shipIds)` | 3 |
| Civilian Self Defense | 1 | Pick a civilian ship. Destroy 3 raiders or 1 heavy raider in its area, then roll: on 2 or less, the civilian ship is destroyed | `CivilianSelfDefenseEvent(civilianShipId, target)` | 3 |
| Resources for Galactica | 1 | Repair up to 1 location and 2 damaged vipers | `ResourcesForGalacticaEvent(location, viperIds)` | 3 |
| Unsavory Connections | 1 | Discard 2 random Skill Cards, draw 2 Treachery, then +1 food or +1 fuel | `UnsavoryConnectionsEvent(resource)` | 4 |
| Consult the Oracle | 1 | Look at the bottom card of a deck, then at the whole Destiny deck. Discard 2 Destiny cards and shuffle it | `ConsultTheOracleEvent(deck)` | 4 |
| Resignation | 1 | Discard any number of Quorum Cards, draw as many, then give the President title to another character | `ResignationEvent(discards)` | 4 |
| Accept Prophecy | 1 | Draw 1 Skill Card of any color. Stays in play: +2 difficulty when the "Admiral's Quarters" targets the President, then discard it | `AcceptProphecyEvent` | 5, 6, 7 |
| Assign Arbitrator | 1 | Draw 2 Politics, give the card to another player. When the "Admiral's Quarters" is activated, the holder may discard it for ±3 difficulty | `AssignArbitratorEvent(holder)` | 5, 6 |
| Assign Chief of Staff | 1 | Draw 2 Politics, give it away. Before cards go into a check, the holder may discard it so Politics counts positive | `AssignChiefOfStaffEvent(holder)` | 5, 6 |
| Probation | 1 | Give it to a player. After that player puts cards into a check, the President may discard it to look at them | `ProbationEvent(holder)` | 5, 6 |
| Assign Mission Specialist | 1 | Draw 2 Politics, give it away. At the next jump the holder draws 3 Destination Cards and chooses, then discards it | `AssignMissionSpecialistEvent(holder)` | 5, 7 |
| Assign Vice President | 1 | Draw 2 Politics, give it away. While the holder isn't President, nobody else can be made President at the "Administration" | `AssignVicePresidentEvent(holder)` | 5, 7 |

Every event has `playerNumber` first. Every card but the ones that stay in play ends in the discard pile. "Presidential
Pardon" prints no "then discard" (see "Open questions").

## Target design

```text
ActionStepEvent, GrantEvent(ACTION), "Hangar Deck"     QuorumChamberEvent          PopularInfluenceEvent
                  |                                          |                              |
                  v                                          v                              v
      Actions.offerAction(game, n)            draw 1, then one(DrawQuorumCardEvent,   draw 2, one(PopularInfluence-
      decision<ActionEvent> when he can:          decision<QuorumCardEvent>,          ChoiceEvent(given, other)...),
        his location's action,                    NoOpEvent)                          then one(decision<that card's
        a Skill Card, a Quorum Card,                                                  event>, DiscardHeldQuorum-
        the President's title action                                                  CardEvent)
                  |                                          |                              |
                  +-------------------+----------------------+------------------------------+
                                      v
                   QuorumCardEvent -> QuorumCardEventProcessor
                   isValid: human, holds the card (his held card, else the President's hand), isValidPlay()
                   process: the card leaves the hand, then resolve(), which ends with the card's fate:
                            discard pile, out of the game, or in play (Game.quorumCardsInPlay, with a holder)
```

- **`QuorumCardEvent extends ActionEvent`** (`event/action/`): an event that plays a Quorum Card. Each card has one
  record in a new `event/action/quorum/`, named after the card, with the card text in its comment, as the Skill Card
  events have. The record is the card: it has no card component, only `playerNumber` and the card's choices.
- **`QuorumCardEventProcessor<T>`**: the base of their processors, like `SkillCardEventProcessor`. `card()` names its
  `QuorumCard`. Its final `isValid()` checks that the player is human and holds the card, then calls `isValidPlay()`.
  Its final `process()` takes the card out of where it was (`TitleOps.play`) and calls `resolve()`.
- **`QuorumCardPlays`** (`event/action/`): `canPlay(game, player, card)`, an exhaustive switch over `QuorumCard` that
  asks each card processor's static `canPlay`, as `SkillCardPlays` does. Cards of later phases map to `false` until
  their phase. `canPlayAny(game, player)` covers the President's hand. `eventClass(card)` is a second exhaustive switch,
  for the decision "Popular Influence" asks.
- **`PresidentTitleActionEvent(playerNumber)`** (`event/action/title/`): the President Title Card's action, draw a
  Quorum Card. The Admiral's and the CAG's actions go beside it later.
- **`Game.quorumCardsInPlay`**: a list of `QuorumCardInPlay(card, holderPlayerNumber)`. The holder is `null` for "Accept
  Prophecy". Two facts change it, see phase 5.

## Decisions

1. **One event per card, with the card's choices on it.** The Skill Cards do it this way. A play is one submit, and
   its processor checks the whole play: the hand, the player, the targets. A choice that depends on what the card
   reveals comes after it, as a decision or an option: the Destiny cards of "Consult the Oracle", the new President
   of "Resignation". Targets can't be offered as plain options of existing events such as `SendToBrigEvent`. Those
   are plain `Event`s, so the engine would ask the current player to choose, who isn't the President when the
   President acts on a grant.
2. **Who plays.**
   - The President, from his hand, as his action (rule 8.4). That includes the President in the "Brig" (4.4.1.2
     keeps "all associated abilities") and as a pilot, who may take another action instead of activating his viper
     (11.2.1).
   - The President through the Quorum Chamber.
   - The player of "Popular Influence", who plays the card he drew.
   - Revealed Cylons never play one (6.1.2.3). They hold no title anyway.
   - The source is checked in one place. The card comes from the player's held card when he has one (phase 8),
     otherwise from the President's hand.
3. **A card is offered and accepted only when its effect can happen**, as for the Skill Cards and the locations. For
   example, "Arrest Order" needs a character who can be sent to the "Brig", "Execute Prisoner" one in the "Brig",
   "Eulogy" a past execution, "Resources for Galactica" something damaged, and "Resignation" another character who can
   be President. That also keeps the rule that a decision always has a legal answer.
4. **A played card is in no pile while it resolves.** It goes where its text says, when the text says it, usually
   last: the discard pile (face up, A2.3.13), out of the game, in play, or to a player. This differs from the Skill
   Cards, which are discarded first (skill decision 5). The fate of "Food Rationing" depends on a roll, and taking a
   card back out of the discard pile would need a fact of its own. `DiscardPlayedQuorumCardEvent(card)` does "then
   discard this card" after a choice or a roll. A processor whose card text ends without a pause records the fate
   itself.
5. **A card that leaves the game is a deck fact.** `DeckFact.CardsRemovedFromGame(deck, cards)` adds the cards to
   `GameView.removedComponents()`. The `DeckId` beside the list gives the codec its card type. The Mutiny Cards that
   leave the game reuse it later.
6. **In-play cards are state with a holder.** `Game.quorumCardsInPlay` holds the roles with their holder and "Accept
   Prophecy" without one. Each type that stays in play has a single copy, so a card names its entry. They are public:
   the cards lie face up in front of the holder. They leave play when they are used, and the holder's leave on
   execution (7.1) and at the end of an Infiltration (6.2.3.5, "without effect"). The Cylon reveal (6.1.1) will call
   the same `TitleOps.discardQuorumCardsGivenTo(playerNumber)`.
7. **A role is used through an optional question at the moment its card names.** Only the holder is asked, except for
   "Probation", where the President decides (A2.3.13.2). The question comes only when the use changes something,
   like the other optional decisions (`one(event, NoOpEvent)`). "Accept Prophecy", the Mission Specialist and the
   Vice President are rules, not questions.
8. **Difficulty and strength changes are skill check facts.** "Declare Emergency" and "Jury Rigged" are derived from
   cards that stay with the check. The in-play cards leave play when used, so the check has to remember them:
   `SkillCheckFact.DifficultyAdjusted(delta)` and `SkillCheckFact.PoliticsCountPositive()`.
9. **The Quorum hand limit is 10 for every character** (4.4.1.1, 8.8). `player.handLimit()` is the Skill Card limit,
   and "Reckless" makes it 8 for Chief Galen Tyrol.
10. **The deck has 29 cards** (A2.3.13).
    - "Arrest Order" x2 (A4.6.1 names it).
    - "Inspirational Speech" x4 and "Food Rationing" x2. A2.3.13 counts 5 cards that raise morale and 4 that raise
      food, with "Eulogy", "Enact Production Quotas" and "Unsavory Connections".
    - "Authorization of Brutal Force" x2 is the sixth extra copy that makes 29. It is a guess: check the physical deck.
    - The Fully Cooperative variant (A4.6) isn't modeled.
11. **Format and schema in every phase that changes them**, as skill decision 13: a new `FORMAT_VERSION` and
    `journal-format.txt` (`-DupdateFormatLock=true`), `./gradlew :backend:exportSchema` and `npm run gen`.
    `Disclosure.CARD_COMPONENTS` marks every new event component that holds a card. Most Quorum events hold none,
    because the record is the card. The ones that do are listed in their phase.

## Phase 1 - Playing a Quorum Card

The deck, the limit, the plumbing, the Quorum Chamber, the title action, and four cards to prove it.

- **The deck first, on its own commit.** It changes every seeded game: a 29-card shuffle takes more random numbers.
  - `DecksHolder.quorum = genericDeck(QuorumCard.class, ARREST_ORDER, AUTHORIZATION_OF_BRUTAL_FORCE, FOOD_RATIONING,
    INSPIRATIONAL_SPEECH, INSPIRATIONAL_SPEECH, INSPIRATIONAL_SPEECH)` (decision 10).
  - Fix what that shifts, before anything else: `TurnLoopTest`, `ScriptedTurns`, `LocationSkillCheckTest`, the
    sandbox e2e. Most compute their inputs from the game, so few should break.
  - `RegressionJournalsTest` still restores the stored journals. `DeckShuffled` sets the draw order from the fact, so
    a stored game keeps its 23 cards.
- **The limit.** `TitleOps.QUORUM_HAND_LIMIT = 10`, used in `DiscardStepEventProcessor` and
  `DiscardQuorumCardsEventProcessor` instead of `player.handLimit()` (decision 9).
- **Drawing when nothing is left.** The Quorum deck and its discard pile can both be empty, with every card in hands,
  in play or out of the game. `DecksHolderView.quorumCardsLeft()` gives the count. `DrawQuorumCardEvent` draws nothing
  then. "Dishonest Tactics" and the Quorum Chamber queue it.
- `event/action/`:
  - `QuorumCardEvent`, `QuorumCardEventProcessor` and `QuorumCardPlays` (see "Target design").
  - `Actions.canTakeCardAction(game, player)`: a Skill Card, a Quorum Card or the title action.
    - `canTakeAction` is "his location's action or `canTakeCardAction`".
    - A pilot's second decision (`decision<ActionEvent>`) uses it instead of `SkillCardPlays.canPlayAction`.
- `PresidentTitleActionEvent(playerNumber)`, an `ActionEvent`. Its processor checks the President, human, with a card
  left to draw, and draws one (`TitleOps.drawQuorumCard`).
- `TitleOps.play(card)` records `PresidentQuorumCardsLost(card)`. The fates:
  - `TitleOps.discardPlayedQuorumCard(card)`: `CardsDiscarded(QUORUM, card)`.
  - `TitleOps.removeQuorumCardFromGame(card)`: `DeckFact.CardsRemovedFromGame(QUORUM, card)` (decision 5), with
    `RemovedComponents.add` and the `FactApplier` case.
  - `DiscardPlayedQuorumCardEvent(card)` (decision 4).
- **Fix the Quorum Chamber** (bug 1). It draws 1, then offers `one(DrawQuorumCardEvent, decision<QuorumCardEvent>,
  NoOpEvent)`: the second draw, a play, or nothing ("you may"). The play option is there only when
  `QuorumCardPlays.canPlayAny` holds after the draw.
  - Delete the empty `event/placeholder/PlayQuorumCardEvent`.
  - `ProcessorRegistryTest.anEventWithoutAProcessorFailsEvenWithoutAsserts` uses `PlaySuperCrisisCardEvent` instead.
- The cards:
  - Enact Production Quotas and Establish Dogsville: `gain`, `lose`, discard.
  - Food Rationing and Inspirational Speech: `rollDie(new FoodRationingResultEvent())` and
    `InspirationalSpeechResultEvent`. They share one helper: on 6 or more, `gain` and the card leaves the game,
    otherwise it is discarded.
- Sandbox: `GiveQuorumCardToPresidentEvent(QuorumCard card)` takes a card out of the deck (`CardsPulled`) into the
  President's hand. Tests and the browser deal known cards with it, like `GiveSkillCardToPlayerEvent`.
- Projections: the event scopes are public, because the record names the card and holds no card component.
  `CardsRemovedFromGame` is public.
- Frontend:
  - `WireSchema` exports the `quorumCard` of each Quorum event, as it does the `location` of a location's action.
  - The `EventBuilder` lists a Quorum event only when the asked player holds its card. That is a plausibility
    filter, not a rule.
  - Labels for the new events and the fact.
- Tests:
  - `QuorumCardEventsTest`: each `QuorumCard` has exactly one `QuorumCardEvent` record, through its processor's
    `card()`, like `SkillCardEventsTest`.
  - Processor tests for the four cards and the title action. `assertInvalid` for a player who isn't President, a
    card not in the hand, and a revealed Cylon.
  - `ActionStepEventProcessorTest`: the President in "Sickbay" now has an action (the title action). A President who
    pilots gets the second decision.
  - The Quorum Chamber's three options, and its play option only with a playable card.
  - Each roll result, with `FakeDie`.
  - `DiscardStepEventProcessorTest` with Chief Galen Tyrol as President: 10 cards, not 8.
  - A new `QuorumCardActionsTest` in `engine/`, like `SkillCardActionsTest`: the President plays through `submit` and
    `choose`, restoring at every pause. Later phases add to it.
  - `FactApplierTest` and the projector tests for `CardsRemovedFromGame`.

## Phase 2 - Choosing a player

- Arrest Order:
  - The target passes `SendToBrigEventProcessor.canBeSentToBrig`.
  - Discard, then `SendToBrigEvent(target)`. Any character may be the target, the President too.
- Execute Prisoner:
  - The target is in the "Brig" and passes `ExecuteEventProcessor.canBeExecuted`.
  - Discard first, then `ExecuteEvent(target)`, so no card is left out of a pile across the new character's
    selection.
- Presidential Pardon:
  - Another character in the "Brig", and a destination that `MoveFromBrigEvent` accepts: a Galactica location that
    isn't hazardous.
  - Then `MoveFromBrigEvent(target, destination)` and the discard.
- Release Cylon Mugshots:
  - Another player with a Loyalty Card.
  - `LookAtLoyaltyCardEvent(n, target)`, which "Detector Sabotage" already stops, then
    `rollDie(new ReleaseCylonMugshotsResultEvent())`. On 3 or less, lose 1 morale. Then discard.
- Titles:
  - `MakeAdmiralEventProcessor.canBecomeAdmiral` and a new `MakeCagEvent` with `canBecomeCag`: human, not a Cylon
    Leader (6.2.3.3), not in the "Brig" (4.4.2.3, 4.4.3.2), not holding the title. `MakeAdmiralEvent` now validates
    with it. Nothing queued it before, so no caller breaks.
  - A `Title` enum (`PRESIDENT`, `ADMIRAL`, `CAG`) in `state/`. The frontend's `Title/` art folder then names an enum.
    `images.test.ts` may need it moved from the list of folders that name none.
- Encourage Mutiny:
  - Another player, not the Admiral, who `canBecomeAdmiral`.
  - `rollDie(new EncourageMutinyResultEvent(target))`. On 3 or more, `MakeAdmiralEvent` if he still can, otherwise
    lose 1 morale. Then discard.
- Presidential Order:
  - `PresidentialOrderEvent(playerNumber, target, Title title)`, with `ADMIRAL` or `CAG`. Any player who can take that
    title, the President too.
  - Then `MakeAdmiralEvent` or `MakeCagEvent`, and the discard.
- Eulogy:
  - `GameFact.MoraleLostToExecution()` sets `GameView.moraleLostToExecution()`.
  - `ExecuteEventProcessor.human()` records it when the morale loss happens. A "Preventative Policy" can make that
    loss 0, so `AbstractOps.loseResource` returns what was lost.
  - The card is playable once the flag is set: +1 morale, discard.
- Projections: `MoraleLostToExecution` and `CagAssigned` are public.
- Tests:
  - Each card through the engine.
  - The President on a grant: an "Executive Order" target who is President plays "Arrest Order", and the President
    is asked the follow-up choices, not the current player.
  - `MakeAdmiralEvent` refuses a Cylon Leader and a character in the "Brig".
  - "Eulogy" refused before any execution, and after one whose morale loss a "Preventative Policy" took.

## Phase 3 - Ships and repairs

- Building blocks:
  - `DestroyHeavyRaiderEvent(shipId)` (`ShipOps.destroy`).
  - `DestroyCenturionEvent(centurionId)`: `GalacticaOps.destroyCenturion`, then the "Major Victory" window
    (`AfterBasestarOrCenturionDestroyed`). `ArmoryResultEventProcessor` queues it instead of doing both itself.
- Authorization of Brutal Force:
  - `shipIds` is either raiders, 3 of them anywhere on the main board or all of them when there are fewer, or 1 heavy
    raider, or 1 centurion on the boarding party track.
  - Then the destroy events, `rollDie(new AuthorizationOfBrutalForceResultEvent())` (on 2 or less, lose 1
    population), and the discard.
- Civilian Self Defense:
  - `CivilianSelfDefenseEvent(playerNumber, civilianShipId, ShipType target)` with `RAIDER` or `HEAVY_RAIDER`, which has
    to be in the civilian ship's area. Raiders are alike, so the event names the type, and
    `DestroyRaidersEventProcessor.destroyRaidersIn(game, area, 3)` picks them.
  - Then `rollDie(new CivilianSelfDefenseResultEvent(civilianShipId))`. On 2 or less, `DestroyCivilianShipEvent`, if
    the ship is still in space. Then discard.
- Resources for Galactica:
  - `location` is `null` or a damaged location. `viperIds` holds 0 to 2 damaged vipers, as "Repair" takes at the
    "Hangar Deck". At least one of them has to be given.
  - `BattlestarOps.repairLocation` and `ShipOps.repair`. The checks are shared with `RepairEventProcessor` through
    static helpers.
- Tests:
  - Each card through the engine.
  - Brutal Force on a centurion opens the "Major Victory" window.
  - Fewer than 3 raiders.
  - The Armory still opens its window, through the new event.
  - Resources for Galactica with only vipers, and with only a location.

## Phase 4 - Cards and decks

- Unsavory Connections:
  - `resource` is `FOOD` or `FUEL`.
  - `DiscardRandomSkillCardsEvent(n, 2)`, `DrawSkillCardsOfColorEvent(n, TREACHERY, 2)` (fewer when fewer are left,
    8.2.2.5), `gain(resource, 1)`, then the discard.
- Consult the Oracle:
  - `deck` is a card deck with a card in it: a Skill deck, Destiny, Quorum, Crisis, Super Crisis, Mutiny or
    Destination. Not Loyalty or Motive (A2.3.13.1), nor the damage tokens or the civilian ships.
  - The player `see`s the bottom card. `DeckView.bottom()` is new, as the review notes.
  - Then he sees the whole Destiny deck, and `PlayerDecisionEvent<DiscardFromDestinyDeckEvent>` follows. Its
    processor already takes `min(2, size)` and shuffles. There is no decision when the Destiny deck is empty. Then the
    Quorum card is discarded.
- Resignation:
  - The player is the President, and another character `canBecomePresident`.
  - `discards` are cards in the hand after "Resignation" left it, maybe none. They are discarded, and that many are
    drawn.
  - Then `PlayerDecisionEvent<MakePresidentEvent>(n)`, and "Resignation" is discarded last. The new President gets
    the hand, which belongs to the title.
  - `ResignationEvent.discards` is public in `Disclosure`: the discard pile is face up.
- Projections: the looks are `CardsSeen`, private to the player.
- Tests:
  - Each card through the engine.
  - Resignation with 0 and with 2 discards, the new President holding the hand afterwards.
  - "Consult the Oracle" refuses the Loyalty deck and an empty deck.

## Phase 5 - Cards in play

- State:
  - `Game.quorumCardsInPlay` (`@Mutates` add and remove), `GameView.quorumCardsInPlay()` and
    `GameView.quorumCardHolder(card)`.
  - `TitleFact.QuorumCardPutInPlay(card, holderPlayerNumber)` and `TitleFact.QuorumCardLeftPlay(card)`, which
    `CardsDiscarded` follows. Both with `FactApplier` cases.
- `TitleOps`:
  - `putInPlay(card, holder)` and `discardFromPlay(card)`.
  - `discardQuorumCardsGivenTo(playerNumber)` (decision 6). `ExecuteEventProcessor.discardCards` calls it for every
    executed player (7.1, step 1 already cites it), and `cylonLeader()` covers the end of an Infiltration (6.2.3.5).
- The plays:
  - Accept Prophecy:
    - It goes into play without a holder.
    - Then `PlayerDecisionEvent<DrawSkillCardsEvent>(n, DRAW_EXACTLY_1_OF_ANY_COLOR)`. The constraint is new, and
      `DrawSkillCardsEventProcessor` supports it beside `DRAW_EXACTLY_2_OF_ANY_COLOR`.
    - It needs a Skill Card left to draw.
  - The four "Assign" cards:
    - `holder` is another human player. An Infiltrating Cylon Leader counts as human, except as Vice President
      (6.2.3.3).
    - The card goes into play with the holder, then `DrawSkillCardsOfColorEvent(n, POLITICS, 2)`.
  - Probation: any human player, the President too, as the card says "any player". It goes into play with the holder.
- Projections: `GameProjection.quorumCardsInPlay`, public. Both facts are public.
- Frontend:
  - The holder's mat shows his cards face up (`CardTile`).
  - "Accept Prophecy" lies beside the President's Quorum pile.
- Tests:
  - Each play through the engine.
  - Execution discards the holder's cards, for a human, a hidden Cylon and an Infiltrating Cylon Leader. The new
    character starts without them.
  - "Assign Vice President" refused for an Infiltrating Leader.
  - `FactApplierTest`, the projector tests, and `GameReplayerTest` through `ScriptedScenario` if it should record
    every kind of fact.

## Phase 6 - Roles in skill checks

- `SkillCheckFact.DifficultyAdjusted(delta)`: `SkillCheck.difficultyAdjustment`, added in
  `DetermineSkillCheckResultEventProcessor.difficulty`. `SkillCheckProjection.difficulty` reads it, so the turn panel
  shows it.
- Accept Prophecy: `AdmiralsQuartersEventProcessor`, after it starts the check. When the target is the President and
  the card is in play, it adjusts by +2 and discards it.
- Assign Arbitrator: also at the "Admiral's Quarters", after "Accept Prophecy" and before the "Political Prowess"
  decisions. The holder gets `one(UseArbitratorEvent(h, +3), UseArbitratorEvent(h, -3), NoOpEvent(h))`. A use
  adjusts the difficulty and discards the card.
- Assign Chief of Staff:
  - `ResolveCardsBeforeSkillCheckEventProcessor` asks the holder just before the players put their cards in:
    `one(UseChiefOfStaffEvent(h), NoOpEvent(h))`. It asks only when Politics isn't one of the check's colors.
  - The use records `SkillCheckFact.PoliticsCountPositive()` and discards the card.
  - `TotalSkillCheckStrengthEventProcessor.countsAsPositive` gets a `POLITICS` case.
  - Super Crisis checks too: their immunity is to character abilities (6.1.1, step 4).
- Probation:
  - `PlayCardsIntoSkillCheckEventProcessor` acts when the holder put at least 1 card in and the President is
    another player. The President is asked `one(UseProbationEvent(president, holder), NoOpEvent(president))`.
  - The use records `SkillCheckFact.CardsLookedAt(playerNumber, targetPlayerNumber, cards)`, which applies nothing,
    like `CardsSeen`. The cards come from the check's `PlayedCard`s of the holder. Then the card is discarded.
  - A `List<SkillCard>` fact is typed, so the codec needs no `DeckId`.
- Projections:
  - `CardsLookedAt` shows the cards to the President, and to everyone else as `Hidden` (a count and the colors).
  - `PoliticsCountPositive` and `DifficultyAdjusted` are public.
  - The turn panel's skill check notes "Politics counts positive".
- Tests: through the engine, in the style of `LocationSkillCheckTest`:
  - An "Admiral's Quarters" on the President with "Accept Prophecy" (+2) and the Arbitrator (-3), and the totals.
  - A Crisis check with the Chief of Staff, where Politics counts positive.
  - Probation: the President's log shows the cards, another viewer's doesn't. No question when the holder put in
    nothing.

## Phase 7 - Roles at the Administration and the jump

- Assign Mission Specialist:
  - `DrawDestinationCardsEventProcessor`: when the card is in play, the holder draws 3 instead of the Admiral's 2,
    sees them, and the card leaves play.
  - `ChooseDestinationEvent(playerNumber, chosen, List<DestinationCard> others)`. The others go to the bottom in the
    order drawn. It is only ever an option the server builds, so its check keeps the cards' sanity and drops the
    Admiral.
  - Helena Cain's "Blind Jump" supersedes it later (A2.1.1.8). Not here.
- Assign Vice President: `ReplaceMutinousPresidentEventProcessor`. When the holder isn't President, nobody else may be
  chosen. It queues `MakePresidentEvent(holder)` when he `canBecomePresident`, and nothing otherwise. That isn't a
  choice, so it needs no decision. `MakePresidentEventProcessor.isValid` stays as it is, because the event is queued
  directly too.
- Accept Prophecy at the "Administration": the todo in `ReplaceMutinousPresidentEventProcessor`.
  - When the President holds a Mutiny Card and "Accept Prophecy" is in play, the President chooses first, between two
    `DefendPresidencyEvent(president, discardAcceptProphecy, administrationPlayerNumber)` options. Both name the
    President, so the engine asks him.
  - Discarding keeps the title. Otherwise the replacement follows (the Vice President's rule, or the Administration
    player's decision).
  - The card's "+2 difficulty at the Administration" has nothing to apply to, because the Daybreak "Administration"
    has no skill check.
- Projections: the three Destination cards are a `CardsSeen` of the holder, as the Admiral's 2 are.
- Tests:
  - A jump with a Mission Specialist: the holder is asked, not the Admiral, chooses from 3, and 2 go to the bottom.
    The next jump is the Admiral's again.
  - The "Administration" with a Vice President, and with the Vice President as President, which applies no rule.
  - "Accept Prophecy" discarded to keep the title, and declined.

## Phase 8 - Popular Influence

The rest of phase 10 of `skill-cards-plan.md`.

- `PlayerPile.QUORUM`: Quorum Cards a player holds outside the President's hand. Only the card "Popular Influence"
  leaves him to play is ever there. `Player.quorumCards` and `PlayerView.quorumCards()`, with `cardType()`.
- `QuorumCardEventProcessor`: the source rule of decision 2. `TitleOps.play` takes the card from the held pile
  (`CardsLost`) when it is there.
- `TitleOps.drawQuorumCards(count)` returns cards that are in no pile yet. The player `see`s them.
- `PopularInfluenceEvent(playerNumber, card)`:
  - A `SkillCardEvent` and an `ActionEvent`, playable when a Quorum Card can be drawn. It draws 2, or 1 when only 1
    is left.
  - The player chooses among the `PopularInfluenceChoiceEvent(n, given, other)` options the server builds, one per
    order. The set collapses two cards of one type.
- `PopularInfluenceChoiceEventProcessor`:
  - `PresidentQuorumCardGained(given)`, then `CardsGained(n, QUORUM, other)`.
  - Then `one(PlayerDecisionEvent<>(n, QuorumCardPlays.eventClass(other)), DiscardHeldQuorumCardEvent(n, other))`.
    The play option is there only when `canPlay(game, player, other)` holds.
  - The decision's class is that card's event, so a President who plays "Popular Influence" can't play another card
    from his hand instead.
- Cards played by someone who isn't President: "you" and "another player" mean the player of the card. "Resignation"
  can't be played: there is no title to give.
- `SkillCardPlays.canPlay(POPULAR_INFLUENCE)` asks the new processor. `SkillCardEventsTest.NOT_BUILT_YET` is empty
  after this.
- Projections:
  - `PlayerProjection.quorumCards`: the player sees the cards, the others count them.
  - `Disclosure`: `PopularInfluenceChoiceEvent.given` and `.other` are private (a choice's options show only to its
    chooser), and `DiscardHeldQuorumCardEvent.card` is public.
- Frontend: the held card on the player's mat. The `EventBuilder` offers that card's event (phase 1's filter reads the
  held pile too).
- Tests, through the engine:
  - A player plays it and plays the other card.
  - A player discards the other card.
  - The President plays it, gives himself one card and plays the other, which comes from the held pile even when his
    hand has the same type.
  - A "Resignation" drawn by a player who isn't President can only be discarded.
  - Only 1 card left to draw.

## Phase 9 - Docs

Each phase already fixes the `CLAUDE.md` lines it makes stale. For example, "Placeholder events" and "Engine" name
`PlayQuorumCardEvent`, and "Actions and Skill Cards" says "Popular Influence" isn't built. This phase adds what is left:

- `CLAUDE.md`:
  - A "Quorum Cards" section: `QuorumCardEvent` and its processor, `QuorumCardPlays`, the fates, the in-play cards
    and where each is used, the title action, "Popular Influence".
  - Roadmap step 4 without the Quorum Cards and the President's action.
  - `QuorumCardActionsTest` under "Testing Patterns".
  - `event/action/quorum/` and `event/action/title/` under "Key Packages".
- `unimplemented-components.md`:
  - The rows of Quorum Cards, Skill Cards, Location actions and Title Card actions (1 of 4).
  - Bugs 1, 6 and 7.
  - The Quorum section.
  - The building blocks this plan built: the remove fact, `MakeCagEvent`, the checks in `MakeAdmiralEvent`, heavy
    raider and centurion destruction, the destination choice, in-play Quorum Cards.
  - The character abilities this unblocks: Laura Roslin's "Skilled Politician", Tory Foster's "Adaptable" and
    "Influential".
- `skill-cards-plan.md`: the status line, phase 10 done.

## Open questions

Each has the choice the plan makes. A different answer changes only its phase.

1. **The 29th card** (decision 10). The second "Authorization of Brutal Force" is inferred: the appendix's counts fix
   the other extra copies, and 29 needs one more. Check the physical deck.
2. **"Eulogy"'s execution.** Any execution earlier in the game counts, not only this turn's. It is a game flag.
3. **"Destroy 3 raiders" with fewer on the board.** It destroys those there are ("Authorization of Brutal Force",
   "Civilian Self Defense"). A card that would destroy nothing isn't offered (decision 3).
4. **"Resources for Galactica"'s location.** It can be a Galactica or a Pegasus location: the card doesn't limit it,
   only its title says Galactica.
5. **"Consult the Oracle"'s decks.** The card decks without Loyalty and Motive. Not the damage tokens or the civilian
   ships, which are tokens. An empty deck can't be chosen.
6. **"Presidential Pardon"** prints no "then discard". It is discarded like every card that doesn't stay in play. The
   destination follows `MoveFromBrigEvent`: not a hazardous location, although the card says "any other location".
7. **"Accept Prophecy" has no holder.** It stays in play when the presidency changes. A President who reveals as a
   Cylon doesn't lose it: 6.1.1's "keep in play" cards are read as the ones given to him.
8. **Role holders are human**, Infiltrating Cylon Leaders included (6.2.3.3 names "Assign Mission Specialist" and
   "Assign Arbitrator"). "Probation" may go to the President himself; its question is then never asked.
9. **Titles from cards skip the "Brig".** "Encourage Mutiny" and "Presidential Order" don't target a character who
   would lose the title at once (4.4.2.3, 4.4.3.2).
10. **The Arbitrator is asked before "Political Prowess".** The card says "when a player activates" the location, so
    it can be used up on a check that "Political Prowess" then skips.
11. **The Mission Specialist's two other cards** go to the bottom in the order drawn. The rules don't say who orders
    them.
12. **Discard-first where nothing depends on it.** "Execute Prisoner" discards before the execution. The card says
    after, and no rule can tell the difference.
13. **One `FORMAT_VERSION` per phase** (decision 11), as the codec's rule says. With no stored game worth keeping, one
    bump for the whole plan would do.

## Not in this plan

- Character abilities that touch Quorum Cards: Laura Roslin's "Skilled Politician", Tory Foster's "Adaptable" and
  "Influential", Helena Cain's "Blind Jump" over a Mission Specialist, and Saul Tigh's "Cylon Hatred" and Helena
  Cain's "Intolerant" at the "Admiral's Quarters".
- The Mutiny Card "Set the Agenda", which draws for the President and looks at his hand. It belongs to the Mutiny
  Cards.
- The Cylon reveal (6.1.1) and an Infiltrating Leader's return to the "Resurrection Ship" (6.2.3.5). Both will call
  `TitleOps.discardQuorumCardsGivenTo`.
- Titles passing when the Admiral or the CAG goes to the "Brig" (4.4.2.3, the todos in `PlayerOps.moveTo` and
  `LeaveBrigEventProcessor`). "Arrest Order" reaches it, as the "Admiral's Quarters" already does.
- The Admiral's and the CAG's title actions, and the nuke launch.
- A Final Five card found by "Release Cylon Mugshots"' look, which comes with the Final Five.
- The Fully Cooperative variant (A4.6.1), Aaron Doral's Ally (Ionian Nebula) and the New Caprica rule 9.8.2.1: out of
  scope.
- Legal-move hints for the frontend. The `EventBuilder` only narrows the Quorum events to the cards held.
