# Jump, Kobol and the end of the game

## Jump and Kobol

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
  Cards and Personal Goals revealed during the game are under [Cylons and Loyalty Cards](cylons-and-loyalty.md#cylons-and-loyalty-cards).

## End of Game and Titles

- `WinChecks` (`event/endgame/`) holds the Cylon wins that happen at once (rule 16.1): a centurion at the end of the
  Boarding Party track, and 6 damaged Galactica locations. The engine asks after every event, and `ScopeClosed` records
  the result with the event's followup, so restore never runs the checks. The resource check stays in `EndTurnEvent`.
- `CylonsWinEvent` ends the game for the Cylons through `EndGame.end` (the `GameEnded` fact), and `GameView.outcome()` gives the winning `Team`,
  which `Finished(winner)` reports, and `GameView.winners()` the players who won. A test script that plays to the end has to end the game, e.g. with a
  `CylonsWinEvent`.
- Titles in the "Brig" (rules 4.4.2.3 and 4.4.3.2): `DetermineNextAdmiralEvent` and `DetermineNextCagEvent` pick the
  highest in line outside the "Brig", and when every human is in it, the highest in line there.
  `LineOfSuccessionSelector.nextInLine` skips revealed Cylons. Each processor's `LINE_OF_SUCCESSION` is the line,
  highest first, which the wire schema exports for the reference's Titles tab. Leaving the "Brig" (`LeaveBrigEvent`,
  `MoveFromBrigEvent`) queues them again while the Admiral or the CAG is still inside (`BrigTitles.afterLeaving`), so
  the one who left takes the title.
- The Admiral's and the CAG's Title Card actions are under [Combat](combat.md), the President's under [Quorum Cards](quorum-and-mutiny-cards.md#quorum-cards).
