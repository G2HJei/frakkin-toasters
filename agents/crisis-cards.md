# Crisis Cards

## Crisis Cards

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
  the chooser, because the engine derives who answers a `OneOf` from its options (see [Engine](engine.md#engine)) and
  most sections name no player. Don't put the effects of a section directly into the `one(...)`. A section a card
  forbids isn't offered: the "nuke token" sections of "Abandon Galactica" and "Build Cylon Detector" need a nuke token.
- `ChooseCrisisSectionEventProcessor.resolve` starts the skill check on the top of a skill check crisis, and otherwise
  asks `followup(card, section)` for the effects of the section. When "Moral Compass" can change the choice, the
  processor offers `one(MoralCompassEvent, ResolveCrisisSectionEvent)` first, which resolve through the same method.
- `choice(card)` applies "Amoral" (only the first section for Tory Foster as current player) and adds "Choose a
  Different Path" (`ChooseADifferentPathEvent`, then `differentPath()`) for a chooser who can use it. Each builder
  serves one card, and gets Romo Lampkin's discards from `DeceitfulDiscards` ("Deceitful": his first is 1 card
  smaller, a `DeceitfulEvent`), as `SkillCheckResultFollowups` does for a Crisis Card's check. Every discard it gives
  is a forced one, which "For Love" may make smaller (`ForcedDiscards`, see [Character Abilities](character-abilities.md)).
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
  `LookAtLoyaltyCardEventProcessor`, and "Legendary Discovery" counts as 1 distance (see [Jump and Kobol](jump-and-endgame.md#jump-and-kobol)).
- "Fulfiller of Prophecy" returns to the Crisis step after the Activate Cylon Ships step (see [Turn Loop](turn-loop.md)).
- The Activate Cylon Ships step activates the icon of the card drawn last. The Cylon ships go through the same events
  as the skill check crises, e.g. `ActivateRaidersEvent`, in `CrisisCardFollowups.activation`.

## Super Crisis Cards

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
