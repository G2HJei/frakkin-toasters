# Turn loop

The setup and the turns are ordinary events in `event/turn/`, so the engine knows nothing about steps. Each is a plain
`Event` about the current player.

`Turn` (`GameView.turn()`, `TurnView`) holds the turn number (player turns, not rounds), the `GameStep`, the current
and acting player, the turn's Crisis Cards and whether it returns to the Crisis step, the `DieRoll`, the open interrupt
window and its cards, the Skill Cards played for their abilities, the "Preventative Policy" loss reductions, the
"Negotiation" hold, the `TurnLimit`s each player used (what is limited to once a turn, e.g. the CAG's pilot action,
`TurnView.hasUsed(n, limit)`) and the character abilities each used (`TurnView.hasUsed(n, ability)`).

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
  offers what `Actions.offerAction` finds (see [Actions and Skill Cards](actions.md)): an `ActionEvent` decision when the player can
  take his location's action or another one (`Actions.canTakeOtherAction`: an "Action:" Skill Card, a Quorum Card, a
  Mutiny Card, a Title Card's action, the Cylon reveal, a Personal Goal's reveal, an Infiltrating Cylon Leader's
  return, an "Action:" of his character sheet), and for a pilot an
  `ActivateViperEvent` decision too. A
  character with neither (e.g. in "Sickbay" without a card, see `LocationEvent.canAct`) gets no Action choice. The
  Movement step offers what `Actions.offerMovement` finds: a `MoveEvent` decision when he can move (not from the
  "Brig"), and a `MovementAbilityEvent` decision when he can play a "Movement:" card or use a "Movement:" ability of
  his sheet, in the "Brig" too (rule 10.4.1). The Movement and Action steps queue `EndOfMovementStepEvent` and
  `EndOfActionStepEvent` after the player's choice, only when he has an ability that acts then (see [Character
  Abilities](character-abilities.md)).
- `DiscardStepEvent` asks every player over the hand limit, clockwise from the current player, with
  `DISCARD_DOWN_TO_HAND_LIMIT`: Skill Cards over `handLimit()`, and the President's Quorum Cards over
  `TitleOps.QUORUM_HAND_LIMIT` (10 for every character, rule 4.4.1.1).
- The Crisis step draws a card, keeps it with the turn and queues `ResolveCrisisCardEvent` through `CrisisCardReveals`
  (see [Crisis Cards](crisis-cards.md#crisis-cards)); Laura Roslin draws 2 and picks one (`ReligiousVisionsEvent`, the other to the bottom). The
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
