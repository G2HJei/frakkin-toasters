# Combat

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
