# Actions and Skill Cards

`event/action/` knows what a player can do now, for the Action step and everything else that gives an action. Every
"Action:" and "Movement:" Skill Card is built, "Popular Influence" with the Quorum Cards
(see [Quorum Cards](quorum-and-mutiny-cards.md#quorum-cards)): its player gives one drawn Quorum Card to the President and holds the other
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
  other effect (see [Combat](combat.md)).
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
