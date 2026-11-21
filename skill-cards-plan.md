# Skill Cards plan

Status (2026-11-21): every phase is done. Phase 10, "Popular Influence", was built as phase 8 of `quorum-cards-plan.md`.

Implements the 19 Skill Card types that nothing plays yet: the 18 cards with an "Action:" or "Movement:" ability, and
"Investigative Committee". It closes the Skill Card part of roadmap step 4 ("Executive Order and the other `Action:`
and `Movement:` skill cards") and the first half of "The blocker" in `unimplemented-components.md`: the Action step
offers more than the action of a location, and the Movement step more than a move.

Every phase ends with the tests green. The plan is done when each of the 19 cards can be played in a game that
`GameEngine.start` began, through `submit` and `choose`, with `restore` giving the same game at every pause, and when an
"Investigative Committee" check shows everyone who put which cards into it.

The card effects below are paraphrases. The images in `materials-sorted/SkillCardType/` and the rulebook in
`materials-sorted/bsg-rules/` were read for this plan. Where they say nothing, the plan says so and makes a choice (see
"Open questions").

## The cards

| Card | Timing | Effect | Phase |
|---|---|---|---|
| Consolidate Power | Action | Draw 2 Skill Cards of any types, also outside the skill set | 1 |
| Build Nuke | Action | The Admiral gains 1 nuke token, 3 at most (11.6.1) | 1 |
| Critical Situation | Movement | Take 1 action. Only 1 "Critical Situation" or "Executive Order" per turn | 2 |
| Preventive Policy (printed "Preventative") | Movement | Name a resource. Its next loss this turn is 1 smaller | 2 |
| Executive Order | Action | Another player moves and then takes 1 action, or takes 2 actions. 1 a turn (A2.3.8.4) | 3 |
| State of Emergency | Action | Lose 1 food. Then each player, clockwise from you, may move or take 1 action | 3 |
| Maximum Firepower | Action, piloting | Your viper attacks up to 4 times | 4 |
| Full Throttle | Movement or Action, piloting | Move your viper to any space area. Then it may attack 1 Cylon ship there | 4 |
| Combat Veteran | Action | Activate 1 unmanned viper, from the Reserves or in space, up to 3 times | 4 |
| Launch Reserves | Action | Put up to 2 unmanned vipers from the Reserves into an area with a piloted viper, then activate them | 4 |
| Launch Scout | Action | Risk a raptor. On 3+, look at the top Crisis or Destination Card and put it on the top or the bottom. Otherwise destroy a raptor | 5 |
| Scout for Fuel (`SCOUTING_FOR_FUEL`) | Action | Risk a raptor. On 4+, gain 1 fuel. Otherwise destroy the raptor | 5 |
| Raptor Specialist | Action | Destroy a raptor to gain an assault raptor, or return a destroyed raptor to the Reserves | 5 |
| Test the Limits | Action | Unless the jump track is on a blue space, advance it 1 and roll. On 5 or less, damage Galactica | 5 |
| Repair | Action | Repair your location, or at the "Hangar Deck" up to 2 damaged vipers | 6 |
| Unorthodox Plan | Action | Take the action of Command, Armory, Weapons Control or Communications, even if it is damaged | 7 |
| Negotiation | Action | Place a basestar in front of Galactica and draw 1 Politics Card. No Cylon ship launches or activates for the rest of the turn | 8 |
| Investigative Committee | Before a skill check | Everyone plays their cards into the check face up. The Destiny cards stay face down | 9 |
| Popular Influence | Action | Draw 2 Quorum Cards, give 1 to the President, then play or discard the other | 10 |

## Target design

```text
ActionStepEvent, GrantEvent(ACTION), "Hangar Deck"      MovementStepEvent
                  |                                            |
                  v                                            v
      Actions.offerAction(game, n)                 Actions.offerMovement(game, n)
      pilot:  one(decision<ActivateViperEvent>,    one(decision<MoveEvent>             if he can move,
                  decision<ActionEvent>*, NoOp)        decision<MovementAbilityEvent>* ,
      others: one(decision<ActionEvent>, NoOp)         NoOp)
              when he can take any action          * only when a card he holds can be played
                  |                                            |
                  v                                            v
      any ActionEvent he submits:                  any MovementAbilityEvent he submits:
      LocationEvent  -> LocationActionProcessor    SkillCardEvent -> SkillCardEventProcessor
      SkillCardEvent -> SkillCardEventProcessor
```

- **`SkillCardEvent`** (`event/action/`): an event that plays a Skill Card from the hand for its printed ability. It has
  `playerNumber()` and `card()`. Each card has one record in `event/action/skillcard/`, named after the card
  (`ExecutiveOrderEvent`), with the card text in its comment, as the other card events have. An "Action:" card
  implements `ActionEvent`, a "Movement:" card `MovementAbilityEvent`, and "Full Throttle" both.
- **`SkillCardEventProcessor<T>`**: the base of their processors, like `LocationActionProcessor`. Its final `isValid()`
  checks that the card has the processor's `type()`, that the player holds it (`holdsSkillCards`) and is human, then
  calls `isValidPlay()`. `isHuman()` covers the rules: revealed Cylons and Cylon Leaders who aren't Infiltrating can't
  use Skill Card abilities (6.1.2.3, 6.1.3.3, 6.2.1), and Infiltrating Leaders can (6.2.3.2). Its final `process()`
  plays the card (`PlayerOps.play`) and then calls `resolve()`.
- **`SkillCardPlays.canPlay(game, player, type)`**: whether a card of that type can be played now. It is an exhaustive
  switch over `SkillCardType` that asks each card processor's static `canPlay`, like `LocationEvent.canAct` does for
  locations, so a new type doesn't compile until it is mapped. Cards that aren't played as actions or movement map to
  `false`, and so do the cards of later phases until their phase comes.
- **`Actions`** (`event/action/`): the one place that knows what a player can do now. `canTakeAction` is "a location
  action (`LocationEvent.canAct`) or a playable action card". `offerAction` and `offerMovement` build the followups
  above. The Action step, the Movement step, the grants and the "Hangar Deck" all use it. The other kinds of actions
  plug in here later: character sheets, Title Cards, Quorum and Mutiny Cards, the Cylon reveal.
- **`GrantEvent(playerNumber, source, grant)`** with `grant` `MOVE`, `ACTION` or `MOVE_OR_ACTION`: a queued event that
  lets a player act out of turn. It sets the acting player (`GameOps.actingPlayer`), checks the rules of its `source`
  card, and offers through `Actions`. `ResetActingPlayerEvent` ends the card.
- **`ViperActivationRoundEvent(playerNumber, viperId, activationsLeft, attacksOnly)`**: one round of a repeated
  activation. It offers the legal activations of that viper as a `one(...)` of concrete
  `GrantedViperActivationEvent`s, each followed by the next round, plus `NoOpEvent` to stop.

## Decisions

1. **One event per card, played through a shared base.** It is the same pattern as the location actions. Every check
   all cards share is written once (the hand, the player, the type) and can't be forgotten.
2. **The interface is the timing.** The engine accepts an answer when `decision.action().isInstance(event)`. So an
   `ActionEvent` card can only answer the Action step and the grants, and a `MovementAbilityEvent` card only the
   Movement step. `SkillCardType` gets `isAction()` and `isMovementAbility()` (sets, like `BEFORE_SKILL_CHECK_CARDS`).
   A test checks that each such type has exactly one `SkillCardEvent` record in the `EventCatalog`.
3. **A card is offered and accepted only when its effect can happen**, as for the locations (Armory needs a
   centurion, Weapons Control a Cylon ship). Examples: Build Nuke needs fewer than 3 nukes, Repair something damaged,
   Launch Scout and Scout for Fuel a raptor in the Reserves (13.5). That also keeps the rule that a decision always
   has a legal answer.
4. **`ActivateViperEvent` stays out of `ActionEvent`.** "Command", the "Hangar Deck" and "Training a Rookie" use it for
   unmanned vipers too, and a non-pilot's `ActivateViperEvent` activates an unmanned viper. As an `ActionEvent`, any
   player could do that as his action. A pilot gets two decisions instead: his viper, or an `ActionEvent` when he has
   a card to play (11.2.1). Location events fail for him anyway, because he is in space.
5. **A played card is discarded before its effect.** `PlayerOps.play(card)` records `CardsLost`,
   `GameFact.SkillCardPlayed(playerNumber, type)` and `CardsDiscarded`. "Declare Emergency" and "Political Prowess"
   already discard first. No rule says when (A2.3.8 only says discarded Skill Cards go face up to their pile).
6. **Per-turn limits read a list on the turn.** `SkillCardPlayed` adds to `TurnView.playedSkillCards()`, and
   `TurnStarted` clears it. "Executive Order" and "Critical Situation" refuse to be played when either is in the list.
   The one Movement ability per turn (8.3.5) needs no state: only the Movement step offers them, once.
7. **Acting out of turn is a queued grant that sets the acting player when it runs.** Grants can then nest. If an
   "Executive Order" target plays "State of Emergency", his second action still starts by making him the acting
   player again. A grant's move is a plain `MoveEvent`, because Movement abilities can't be used outside your own
   Movement step (8.3.5). The grant checks its card's rules when it runs:
   - "Executive Order": the target has to be human still, and the turn not ended early. A target who reveals himself
     as his first action loses the second, and the card stops if the current player is executed (A2.3.8.4, A2.3.8.6).
     This works on its own once the Cylon reveal exists.
   - "State of Emergency" always resolves completely, and includes Cylon players (A2.3.8.6).
8. **Repeated activations are offered as concrete options.** The server lists the legal activations of the viper, and
   the next round carries the viper's id and the count left. Clients answer a `one(...)` by index, so they can't
   switch vipers or add an attack. This follows the Validation rule "offer the legal options as a `one(...)`". After
   a launch, the viper is the one `GalacticaOps.launchHumanFighter` takes: the lowest id of that type in the Reserves.
   `GrantedViperActivationEvent` is only ever such an option, never the answer to a decision. Its processor still
   checks the activation, because queued events are checked too.
9. **"Unorthodox Plan" works through a constraint.** `EventConstraint.ANYWHERE_EVEN_IF_DAMAGED` goes on the four
   location events. `GameEngine` replaces a submitted event's constraints with the decision's (line 144), so only the
   decision the card queues can carry it. The Action step's own `ActionEvent` decision clears it.
10. **"Preventive Policy" applies in the ops.** Resources are lost on more than ten paths: `GalacticaOps`, the FUEL and
    FOOD damage tokens, the civilian ships in `ShipOps`. All of them go through one `AbstractOps.loseResource`, which
    takes a pending reduction off the next loss (13.2: "whenever a resource is lost for any reason"). The sandbox's
    `ChangeResourceEvent` bypasses it.
11. **"Negotiation" stops what launches or activates.** It sets a turn flag. Every processor that launches or activates
    Cylon ships checks it, so a Crisis Card's text activation stops too, not only the Activate Cylon Ships step.
    Placing ships isn't launching, so `PlaceShipsEvent` and the Crisis Cards' "place" effects still happen.
12. **"Investigative Committee" adds a public fact of what was shown.** The cards still go into the face-down pile, so
    dividing and totaling don't change. `SkillCheckFact.CardsShown(playerNumber, cards)` tells everyone who played
    what. It follows backend decision 8: what a player learns is a fact. The Destiny cards stay face down (card text).
13. **Format and schema in every phase that changes them.** A new event or fact means a new `FORMAT_VERSION` and
    `journal-format.txt` (`-DupdateFormatLock=true`), `./gradlew :backend:exportSchema` and `npm run gen`. Every new
    event component that holds a card gets marked in `Disclosure.CARD_COMPONENTS`. A played Skill Card is public.

## Phase 1 - Playing a card as an action

The plumbing, and two cards to prove it.

- `SkillCardType`: `isAction()` and `isMovementAbility()`.
- `event/action/`:
  - `SkillCardEvent` and `SkillCardEventProcessor`.
  - `SkillCardPlays`: `canPlay(game, player, type)`, plus `canPlayAction(game, player)` and
    `canPlayMovementAbility(game, player)` over the distinct types in the hand.
  - `Actions.canTakeAction` and `Actions.offerAction` (see "Target design").
- `PlayerOps.play(SkillCard)`, `GameFact.SkillCardPlayed`, `Turn.playedSkillCards` (`@Mutates`), cleared by
  `TurnStarted`, and the `FactApplier` case.
- `ActionStepEventProcessor` keeps its early exits (the turn ended early, a character off the board) and then returns
  `Actions.offerAction(game, current)`. Its comment about "anything with 'Action:' on it" goes. Effects:
  - A player in "Sickbay", or at a damaged location, gets an action when he holds a playable card.
  - A pilot gets the second decision of decision 4.
  - A revealed Cylon still gets only his location's action.
- `HangarDeckEventProcessor`: its "You may then take 1 more action" becomes `Actions.offerAction` for the launched pilot,
  so he may play a card instead of activating his viper (11.2.1).
- The cards:
  - Consolidate Power: `PlayerDecisionEvent<DrawSkillCardsEvent>` with `DRAW_EXACTLY_2_OF_ANY_COLOR`, as "Consult the
    Hybrid" does.
  - Build Nuke: `GameFact.NukeGained`, `Game.gainNuke` (`@Mutates`) and `GameOps.gainNuke`. It can be played below 3
    nukes (11.6.1, A2.5.5).
- Projections: `SkillCardPlayed` and `NukeGained` are public. The frontend needs nothing: the `EventBuilder` already
  lists every concrete `ActionEvent`, and its `SkillCard` field offers the hand.
- Tests:
  - Processor tests for both cards, and `assertInvalid` for a revealed Cylon, a card not in the hand, and a card of
    another type.
  - `ActionStepEventProcessorTest`: the new offers (a pilot with and without a card, "Sickbay" with a card, a revealed
    Cylon with a card).
  - `HangarDeckEventProcessorTest`.
  - A new `SkillCardActionsTest` in `engine/` that deals the cards with the test-only `GiveSkillCardEvent` and plays
    them through `submit` and `choose`, restoring at every pause. Later phases add to it.
  - The catalog test of decision 2, and `FactApplierTest` for the two facts.
  - `TurnLoopTest` and `ScriptedTurns` choose options by value, so an extra option doesn't break them. A new pause would:
    a player who had no Action choice before, e.g. in "Sickbay", and now holds a playable card. The script then answers
    the new choice with its `NoOpEvent`.

## Phase 2 - Movement abilities

- `MovementAbilityEvent extends PlayerEvent`.
- `Actions.canMove`: on the board and not in the "Brig" (9.2).
- `Actions.offerMovement`: a `MoveEvent` decision when he can move, a `MovementAbilityEvent` decision when a card can
  be played, and `NoOpEvent`. `MovementStepEventProcessor` uses it.
  - A character in the "Brig" may now use a Movement ability (10.4.1, 8.4.1).
  - The `MoveEvent` decision stays as it is, so the board's click-to-move (`boardMoves.ts`) keeps working.
- Critical Situation:
  - Refused when `playedSkillCards()` holds a "Critical Situation" or an "Executive Order". It is a static check that
    Executive Order reuses in phase 3.
  - `resolve()` returns `Actions.offerAction(game, n)`. It is computed after the card left the hand, so the action can't
    be the same card.
  - The card's player is the current player, so it needs no grant.
- Preventive Policy:
  - `PreventivePolicyEvent(playerNumber, card, Resource resource)`.
  - `GameFact.LossReductionAdded(resource)` adds the resource to the turn's reductions. `GameFact.LossReduced(resource)`
    uses it up. `TurnStarted` clears them.
  - `AbstractOps.loseResource(resource, amount)` replaces every direct `ResourceDecreased`. It records `LossReduced`
    and then the smaller loss, or no loss when nothing is left.
  - `ChangeResourceEvent` keeps a raw path, so the sandbox doesn't use up a reduction.
- Tests:
  - The Movement step's offers, including a character in the "Brig" with a Movement card.
  - Critical Situation through the engine into a location action.
  - Preventive Policy on a Crisis Card loss, on a destroyed civilian ship, and on a FUEL damage token. Only the first
    loss is reduced, a loss of 1 becomes none, and the reduction is gone the next turn. Plus op tests with the facts.

## Phase 3 - Acting out of turn

- `GrantEvent(playerNumber, SkillCardType source, Grant grant)` and `ResetActingPlayerEvent` in `event/action/`, per
  decision 7.
  - A grant whose player can do nothing it grants does nothing.
  - `GameOps.actingPlayer` is recorded only when the acting player changes.
- Executive Order:
  - `ExecutiveOrderEvent(playerNumber, card, Integer targetPlayerNumber)`.
  - The target is another player, human (A2.3.8.4, A2.3.8.6), who can move or act. The limit is shared with Critical
    Situation.
  - The followup is `all(one(all(GrantEvent(t, MOVE), GrantEvent(t, ACTION)), all(GrantEvent(t, ACTION),
    GrantEvent(t, ACTION))), ResetActingPlayerEvent)`. The first option is offered only when the target can move, e.g.
    not from the "Brig".
  - The options name the target, so he chooses (see "Engine").
  - A target who pilots moves his viper and activates it as his action, as in his own turn (no ruling was found; see
    "Open questions").
- State of Emergency:
  - `StateOfEmergencyEvent(playerNumber, card)`.
  - `all(lose(FOOD, 1), GrantEvent(p, MOVE_OR_ACTION) for each p in playersFrom(game, playerNumber),
    ResetActingPlayerEvent)`. Each grant offers one choice: a move, his action options, or a pass.
  - Revealed Cylons take part (A2.3.8.6). Their action is still only their location's (6.1.3.3).
- Projections: `GameProjection.actingPlayer` already exists. The frontend's input panel already names the player who
  is asked.
- Tests, through the engine, restoring at every pause:
  - EO: the target moves then acts, or acts twice. A target in the "Brig" is offered only the two actions.
  - EO is refused for a revealed Cylon, for yourself, and after a Critical Situation, and a second EO in the turn is
    refused.
  - An EO target plays State of Emergency: the acting player is right at every pause, and the current player at the
    end.
  - SoE with a revealed Cylon among the players.
  - EO stops when the current player is executed during the target's first action. SoE goes on.
  - `ScopeActor` logs the grants' scopes under the target.

## Phase 4 - Vipers

- `ActivateViperEventProcessor.activations(game, fighter, attacksOnly)`: every legal activation of a fighter in space.
  That is the moves (1 area, 2 for a Mark VII), the attacks on the Cylon ships in its area, and the escorts.
  `isValidActivation` stays the check.
- `ViperActivationRoundEvent` and `GrantedViperActivationEvent` (decision 8).
  - A round does nothing when the viper is gone or nothing is legal.
  - A `GrantedViperActivationEvent` takes the player's own viper or an unmanned one (11.1: never one another player
    pilots) and runs `ActivateViperEventProcessor.activate`.
- Maximum Firepower: the player pilots and a Cylon ship is in his area. Then `ViperActivationRoundEvent(n, own, 4,
  true)`.
- Full Throttle:
  - `FullThrottleEvent(playerNumber, card, Location destination)`, an `ActionEvent` and a `MovementAbilityEvent`.
  - `destination` is any Galactica space area other than his own, with no distance limit, or `null` to stay.
  - Then `ViperActivationRoundEvent(n, own, 1, true)`: the attack is optional, "even if you didn't move".
- Combat Veteran:
  - `CombatVeteranEvent(playerNumber, card, ViperActivation first)`. `first` is a legal activation of an unmanned
    viper, and a launch from the Reserves counts (`isValidActivation(..., unmanned, mayLaunch = true)`).
  - It runs `first`, then `ViperActivationRoundEvent(n, viper, 2, false)`.
  - A pilot may play it too.
- Launch Reserves:
  - `LaunchReservesEvent(playerNumber, card, Location area, List<ShipType> shipTypes)`.
  - 1 or 2 fighters that are in the Reserves (vipers, Mark VIIs, assault raptors, which count as vipers, 11.3.2.2).
    The area holds a piloted viper.
  - It launches each with `GalacticaOps.launchHumanFighter`, then queues `ViperActivationRoundEvent(n, id, 1, false)`
    per fighter.
- Tests:
  - Each card, through the engine.
  - A round re-offers after its attack destroyed the target, and stops when no Cylon ship is left.
  - A piloted attack still opens the "Best of the Best" window.
  - `GrantedViperActivationEvent` refuses another player's viper.
  - Combat Veteran's rounds follow the launched viper.

## Phase 5 - Raptors, dice and the jump track

- `LookAtTopCardEvent(playerNumber, DeckId deck)` for `CRISIS` or `DESTINATION`: it draws, `see`s and offers
  `one(place on top, place on bottom)` with the existing `Place...` events. The "Human Fleet" processors could use it
  later.
- Launch Scout: `rollDie(new LaunchScoutResultEvent(n))`. On 3 or more, the player picks a deck: `one(LookAtTopCardEvent(n,
  CRISIS), LookAtTopCardEvent(n, DESTINATION))`. Otherwise `DestroyRaptorEvent`.
- Scout for Fuel: `rollDie(new ScoutForFuelResultEvent(n))`. On 4 or more, `gain(FUEL, 1)`. Otherwise
  `DestroyRaptorEvent`.
- Raptor Specialist:
  - `RaptorSpecialistEvent(playerNumber, card, Choice choice)` with `BUILD_ASSAULT_RAPTOR` or `RETURN_RAPTOR`.
  - There are 4 assault raptors (A2.4.4), and the board has one (id 21). Ids 22 to 24 start in a new
    `ShipZone.HUMAN_SUPPLY`, with a `ShipZones` case.
  - Building destroys a raptor from the Reserves and moves an assault raptor from the supply to the Reserves
    (11.3.2.1). A destroyed assault raptor stays removed (11.7.1.2).
  - Returning uses `ShipOps.repair` on a destroyed raptor.
- Test the Limits:
  - It can be played on `START`, `POSITION_1` and `POSITION_2`. The blue spaces are `MINUS_3` and `MINUS_1`: the
    rulebook has no list, so this comes from the track images.
  - `IncreaseJumpPreparationEvent`, which can't jump from those spaces, then `rollDie(new TestTheLimitsResultEvent(n))`.
  - On 5 or less, the rule 11.7.2 choice that `AttackGalacticaResultEventProcessor` offers:
    `one(DamageGalacticaEvent, DamagePegasusEvent)`. Move it into a `FollowupUtils.damageGalactica()` that both use.
- Projections: the look is a `CardsSeen`, as at the "Human Fleet". The `HUMAN_SUPPLY` ships don't show on the board.
- Tests:
  - Each roll result, through the engine, with `FakeDie` in the processor tests.
  - An assault raptor can't be built without a raptor in the Reserves, or with the supply empty.
  - Test the Limits is refused on a blue space.
  - `RegressionJournalsTest` still restores the stored journals with the new ships.

## Phase 6 - Repair

- Repairing a location (11.7.2.2):
  - `BoardFact.LocationRepaired(location)`, `BattlestarBoard.repair(location)` (`@Mutates`) and the `FactApplier` case.
  - `BattlestarOps.repairLocation(location)` records it, then puts the damage token back on its pile
    (`CardsAddedOnTop` of `GALACTICA_DAMAGE` or `PEGASUS_DAMAGE`) and shuffles it.
- Repair:
  - `RepairEvent(playerNumber, card, List<Integer> viperIds)`.
  - Empty means the player's location, which has to be damaged.
  - Otherwise 1 or 2 different damaged vipers (`damagedShips()`), and the player at the "Hangar Deck". `ShipOps.repair`
    puts each back in the Reserves.
- Pegasus locations repair the same way. Nothing destroys Pegasus yet (rule 11.7.2.4 isn't built), so a repair assumes
  the board is in play.
- Projections: `LocationRepaired` is public. The board's "damaged" chip goes with the projection.
- Tests:
  - `FactApplierTest`, and an op test with the facts and the token back in the shuffled pile.
  - The processor's three cases and its refusals.
  - The location's action can be taken again (`LocationEvent.canAct`).

## Phase 7 - Unorthodox Plan

- **Fix "Communications" first**, because the card can take its action (bug 1 in `unimplemented-components.md`).
  `MoveCivilianShipEvent` gets a processor that moves the ship to the adjacent area it names (`ShipOps.moveTo`), and
  its field `civilianShipIt` becomes `civilianShipId`. `CommunicationsEventProcessorTest` plays a move.
- `EventConstraint.ANYWHERE_EVEN_IF_DAMAGED`. `CommandEvent`, `ArmoryEvent`, `WeaponsControlEvent` and
  `CommunicationsEvent` implement `ConstrainedEvent` with a `boolean anywhere` component.
- `LocationEvent`:
  - Splits the damage check out of `canAct`, so `canActIgnoringDamage(game, player, location)` keeps each location's
    own precondition.
  - `LocationActionProcessor.isValid()` takes `anywhere` ? `canActIgnoringDamage` : at the location and `canAct`.
- Unorthodox Plan:
  - `UnorthodoxPlanEvent(playerNumber, card, Location location)`: one of the four, whose precondition holds. Command's
    is `canActivateUnmanned`.
  - It queues `PlayerDecisionEvent<>(n, <that location's event>, ANYWHERE_EVEN_IF_DAMAGED)`.
  - The player doesn't have to be at the location: the card doesn't ask for it.
- Tests:
  - Each of the four from elsewhere and while damaged.
  - A `CommandEvent` with `anywhere = true`, submitted in the Action step by a player who isn't at "Command", is
    refused, because the engine clears the flag.
  - A Communications move through the engine.

## Phase 8 - Negotiation

- `GameFact.CylonShipsHeld()` sets `Turn.cylonShipsHeld`, and `TurnStarted` clears it.
- Negotiation: `NegotiationEvent(playerNumber, card)`:
  - It records the hold, then queues `PlaceShipsEvent(1, 0, 0)` and `DrawSkillCardsOfColorEvent(n, POLITICS, 1)`.
  - The basestar comes from the supply or the Cylon Fleet board, or isn't placed (11.4.2).
- These processors do nothing while the ships are held:
  - `ActivateRaidersEvent`, `ActivateRaiderEvent`, `LaunchRaidersEvent`, `ActivateHeavyRaidersAndCenturionsEvent`,
    `ActivateCylonBasestarsEvent` and `ActivateCylonBasestarEvent`.
  - The "Cylon Fleet" location gets a static `canAct` that is false while the ships are held: its activations would do
    nothing.
- Tests:
  - After Negotiation, the Activate Cylon Ships step and a Crisis Card's activation leave the ships where they are.
  - A placement still places.
  - The next turn activates again.

## Phase 9 - Investigative Committee

- `SkillCheckView.investigated()` is true when the revealed cards played before the check hold an "Investigative
  Committee". It is derived and needs no fact, because those cards stay with the check until `EndSkillCheckEvent`.
- `SkillCheckFact.CardsShown(playerNumber, cards)` adds to `SkillCheck.shownCards(playerNumber)`, and the
  `FactApplier` case. `PlayCardsIntoSkillCheckEventProcessor` decides: after `playCards`, an investigated check calls
  `SkillCheckOps.show(playerNumber, cards)`. The Destiny cards are dealt before anyone plays and aren't shown.
- Projections:
  - `SkillCheckProjection.shownCards` (player and cards, face up for everyone).
  - `LiveUpdateProjector` shows `CardsShown`. The scope of `PlayCardsIntoSkillCheckEvent` keeps its fields hidden
    (`Disclosure` is per type), and the fact carries the cards.
- Frontend: the skill check in `table/turn/TurnPanel.tsx` lists the shown cards under each player, with a test. Then
  the schema and `npm run gen`.
- Tests:
  - Projector tests: another player sees the cards and who played them, and the Destiny cards stay a count.
  - A check through the engine, in the style of `LocationSkillCheckTest`, with an Investigative Committee played before
    it.
- Chief Galen Tyrol's "Blind Devotion" on such a check (A2.1.1.3) waits for the character abilities.

## Phase 10 - Popular Influence

This phase waits for Quorum Cards that can be played: `PlayQuorumCardEvent` without a processor is bug 2 in
`unimplemented-components.md`, and the Quorum Cards are their own plan. Until then `canPlay` stays `false`, so the card
is never offered.

- `TitleOps.drawQuorumCards(count)` returns cards that are in no pile yet. The player `see`s them.
- `PopularInfluenceEvent(playerNumber, card)` draws 2. The player then chooses between the two
  `GiveQuorumCardEvent(n, given, other)` options the server builds. A choice's options show only to its chooser
  unless they are public, so the others don't see them.
- The given card goes to the President's hand (`PresidentQuorumCardGained`). The player of the card then plays the
  other (`PlayQuorumCardEvent`) or discards it. The card text lets him play it, although rule 4.4.1.1 leaves Quorum
  Cards to the President.
- Tests: a President who plays it himself, a player who discards, a player who plays.

## Phase 11 - Docs

Each phase already fixes the `CLAUDE.md` lines it makes stale. For example, "Turn Loop" says a pilot gets an
`ActivateViperEvent` decision instead of a location action. This phase adds what is left:

- `CLAUDE.md`:
  - An "Actions and Skill Cards" section: `Actions`, `SkillCardEvent` and its processor, `SkillCardPlays`, the grants,
    the viper rounds.
  - Roadmap step 4 without the Skill Cards.
  - `SkillCardActionsTest` under "Testing Patterns".
- `unimplemented-components.md`:
  - The Skill Cards row and section.
  - "The blocker", with what is still missing: the other kinds of actions.
  - The Communications bug.
  - The building blocks this plan built: repairing a location, gaining a nuke, more assault raptors, the
    once-per-turn limit across cards.

## Open questions

Each has the choice the plan makes. A different answer changes only its phase.

1. **Unplayable when useless** (decision 3). For example, Build Nuke with 3 nukes, or Repair with nothing damaged. The
   cards say nothing. A player who wants to throw a card away can't use this to do it.
2. **Consolidate Power and Treachery.** No rule says whether "any type" includes Treachery. The plan reuses
   `DRAW_EXACTLY_2_OF_ANY_COLOR`, which allows it, as for "Consult the Hybrid".
3. **Combat Veteran from the Reserves.** The launch counts as the first of the 3 activations. This is inferred from
   11.1.1: a viper in the Reserves can only be launched.
4. **Launch Reserves activates each viper once.** "Then activate those" doesn't give a number.
5. **Unorthodox Plan from anywhere.** The card doesn't ask the player to be at the location.
6. **Negotiation's reach.** Whole activations are skipped, placements aren't. No clarification exists.
7. **Preventive Policy's "next loss".** It is any loss of that resource, the first one only: civilian ships, damage
   tokens and the FTL Control population loss too (13.2).
8. **An Executive Order target in the "Brig" or in a viper.** He is allowed, with his usual limits. No ruling was
   found.
9. **Popular Influence.** It waits for playable Quorum Cards. The player of the card plays the other Quorum Card.
10. **One `FORMAT_VERSION` per phase** (decision 13), as the codec's rule says. With no stored game worth keeping, one
    bump for the whole plan would do.

## Not in this plan

- The other kinds of actions and Movement abilities: character sheets, Title Cards, Quorum and Mutiny Cards, the Cylon
  reveal, the Personal Goal reveal, a Cylon Leader's return to the "Resurrection Ship". `Actions` is where they plug
  in.
- Playing Quorum Cards, beyond what Popular Influence waits for.
- Character abilities that touch these cards, e.g. "Blind Devotion" on an Investigative Committee check (A2.1.1.3).
- The legal answers of a decision as hints for the frontend. The `EventBuilder` lists the new events, as it does the
  location actions.
- Pegasus destruction (rule 11.7.2.4), and the other bugs in `unimplemented-components.md` except Communications.
- The New Caprica and Ionian Nebula rulings on these cards (8.4.2, 8.5.4, 8.3.6.2): out of scope.
- A Stranded Karl "Helo" Agathon still takes no actions while off the board, card actions included.
