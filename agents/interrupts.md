# Interrupt Cards and die rolls

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
  player again when he has a character ability for it (`RollAbilities.canUse`, see [Character Abilities](character-abilities.md)). The
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
