# Cylons, loyalty and execution

## Cylons and Loyalty Cards

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
  `AnytimeEvent` (see [Engine](engine.md#engine)), while its requirement is met (`MotiveCards.canReveal`: the cards that say "if the game
  is over" wait for the end, rule 6.2.2.1). A revealed card stays revealed, and `EndGame.end` reveals at the end what
  is left (see [Jump and Kobol](jump-and-endgame.md#jump-and-kobol)).

## Execution

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
- The executed current player's turn ends early (see [Turn Loop](turn-loop.md)).
- Until he has chosen his new character the player plays none: `isHuman()` is false, so no effect on the humans
  reaches him, e.g. the discards of a Final Five card that another execution reveals in between.
