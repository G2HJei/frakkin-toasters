# Fuzzer report

Random games played by `./gradlew :core:fuzz` (see `fuzzer-plan.md`). The block between the markers is rewritten by
every run; "Fixes" and "Questions" are written by hand.

<!-- fuzz:begin -->

## Last run

2026-11-22 16:05, `13b4c28` (12 changed files), seeds 1-10000, 3-7 players, 3m 14s.

| Players | Games | Finished | Failed | Humans won | Cylons won | Rounds (median / max) |
|---|---|---|---|---|---|---|
| 3 | 10000 | 9734 | 266 | 3 | 9731 | 9 / 50 |
| 4 | 10000 | 9978 | 22 | 0 | 9978 | 8 / 50 |
| 5 | 10000 | 9998 | 2 | 3 | 9995 | 5 / 46 |
| 6 | 10000 | 10000 | 0 | 0 | 10000 | 4 / 38 |
| 7 | 10000 | 10000 | 0 | 2 | 9998 | 3 / 31 |
| All | 50000 | 49710 | 290 | 8 | 49702 | 5 / 50 |

### Endings

| Ending | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|
| Fuel at 0 | 1176 | 1201 | 1258 | 1325 | 1360 |
| Food at 0 | 602 | 497 | 506 | 469 | 466 |
| Morale at 0 | 4206 | 4100 | 4018 | 4037 | 4046 |
| Population at 0 | 2381 | 2749 | 2770 | 2700 | 2704 |
| 6 damaged Galactica locations | 283 | 279 | 282 | 275 | 299 |
| Centurions at the end of the Boarding Party track | 909 | 948 | 958 | 971 | 905 |
| No character left to choose | 0 | 0 | 0 | 0 | 0 |
| Kobol, the humans win | 3 | 0 | 3 | 0 | 2 |
| Kobol, a resource at 0 after the Personal Goals | 174 | 204 | 203 | 223 | 218 |

### Findings

| ID | Kind | What | Games | Example | Status |
|---|---|---|---|---|---|
| F-e700fd | NO_END | No ending after 50 rounds | 290 | 3p seed 128 | question |

- **F-e700fd**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=128`, trace `core/build/fuzz/F-e700fd-3p-128.txt`. Turn 153, `RECEIVE_SKILLS`, player 3 asked for `DrawSkillCardsEvent [DRAW_EXACTLY_5]`.

### Watch

- 4 players: the humans never won in 10000 games.
- 6 players: the humans never won in 10000 games.
- No "No character left to choose" ending in 50000 games.

<details><summary>Event types that never ran (1)</summary>

`DiscardQuorumCardsEvent`

</details>

### History

| Run | Commit | Games | Failed | Open findings |
|---|---|---|---|---|
| 2026-11-22 16:05 | `13b4c28` | 50000 | 290 | 0 |
| 2026-11-22 15:59 | `13b4c28` | 50000 | 293 | 2 |
| 2026-11-22 13:39 | `569cf7e` | 50000 | 275 | 0 |
| 2026-11-22 13:08 | `d17eb9a` | 500 | 2 | 0 |
| 2026-11-22 13:06 | `d17eb9a` | 500 | 2 | 0 |
| 2026-11-22 13:04 | `d17eb9a` | 500 | 3 | 1 |
| 2026-11-22 12:45 | `d17eb9a` | 500 | 5 | 4 |
| 2026-11-22 12:43 | `5534026` | 100 | 1 | 0 |
| 2026-11-22 12:38 | `5534026` | 100 | 2 | 2 |
| 2026-11-22 12:31 | `5534026` | 100 | 4 | 2 |
| 2026-11-22 12:08 | `f043d8c` | 100 | 38 | 23 |
| 2026-11-22 09:43 | `b758639` | 100 | 38 | 23 |
| 2026-11-22 09:37 | `b758639` | 100 | 38 | 15 |
| 2026-11-22 09:37 | `b758639` | 100 | 38 | 16 |

<!-- fuzz:end -->

## Fixes

### F-7c295f, F-a6e77d, F-ca1339 - DEAD_OPTION - fixed 2026-11-22

- Cause: `playOrPassDecisions` built `one(decision, pass)` for every player when the step began, holder or not, so a
  player without "Declare Emergency", "Change of Plans" or "Political Prowess" could pick a play with no answer. Built
  up front, the offers also went stale: after one player played the card, a second holder could still pick a play
  the check no longer allowed (a real deadlock: nothing he submits is accepted).
- Fix: each player's offer is an `OfferPlayOrPassEvent(n, card)` that builds his choice when it runs: the play or the
  pass while he holds the card and `OfferPlayOrPassEventProcessor.canPlay` allows it, otherwise the pass alone, as a
  one-option `OneOf` so that he is still asked (decision 3). The three processors share their rule as a static
  `canPlay`. New event: `FORMAT_VERSION` 38.
- Tests: `OfferPlayOrPassEventProcessorTest`, `LocationSkillCheckTest` (a holder's offer and the others' pass),
  `FollowupUtilsTest`, `MakeSkillCheckEventProcessorTest`, `TotalSkillCheckStrengthEventProcessorTest`,
  `DetermineSkillCheckResultEventProcessorTest`. No journal: the offers are queued in the journal, so a restore offers
  the dead option whatever the fix.
- Files: `OfferPlayOrPassEvent`, `OfferPlayOrPassEventProcessor`, `FollowupUtils`, `MakeSkillCheckEventProcessor`,
  `TotalSkillCheckStrengthEventProcessor`, `DetermineSkillCheckResultEventProcessor`, the three card processors,
  `JournalCodec`, `journal-format.txt`, `scripted-turns.json`, `schema.json`, `types.ts`.

### F-ea9336, F-d95b59, F-db09a4, F-369710, F-1c6b6e, F-f39319 - INPUT_CRASH - fixed 2026-11-22

- Cause: `isValid` threw on a null card: `card.type()` in "Declare Emergency", "Change of Plans" and "Political
  Prowess", and `List.copyOf(...).contains(null)` in `Discard1MutinyCardEvent`, `DiscardDownTo1MutinyCardEvent` and
  `TiebreakCardsBeforeSkillCheckEvent` (an immutable list throws on a null element).
- Fix: the six checks reject a null card, and the tiebreak a null list or a list holding a null.
- Tests: `rejectsNoCard` in the six processor tests (`rejectsNoList` too for the tiebreak), journals
  `fuzz-4p-14.json` (F-1c6b6e) and `fuzz-6p-12.json` (F-f39319).
- Files: the six processors.

### F-896a90, F-dafda6, F-0634f1, F-7f36f4 - CRASH - fixed 2026-11-22

- Cause: "Bait" placed a civilian ship from an empty civilian ship deck (`Deck.top` past the end). Every other
  placement checks `civilianShipsLeft()` first.
- Fix: `BaitEventProcessor` places one only when one is left.
- Tests: `BaitEventProcessorTest.nothingWhenNoCivilianShipIsLeft`, journal `fuzz-3p-11.json`.
- Files: `BaitEventProcessor`.

### F-5eb977, F-104b07, F-17bf33 - CRASH - fixed 2026-11-22

- Cause: "Reckless" and "A Better Machine" drew from the Treachery deck when its draw and discard piles were empty
  (every Treachery card in a hand).
- Fix: a color with no cards left can't be drawn (rule 8.2.2.5): `SkillCheckOps.shuffleIntoDestinyDeck` draws what is
  left, `DrawAndResolveRecklessCardsEventProcessor` draws nothing without a Treachery card.
- Tests: `ABetterMachineEventProcessorTest.shufflesInOnlyTheTreacheryCardsLeft`,
  `DrawAndResolveRecklessCardsEventProcessorTest.nothingWhenNoTreacheryCardIsLeft`, journals `fuzz-7p-3.json`,
  `fuzz-7p-14.json`.
- Files: `SkillCheckOps`, `DrawAndResolveRecklessCardsEventProcessor`.

### F-2928df, F-86ead5 - CRASH, INPUT_CRASH - fixed 2026-11-22

- Cause: `MoveEventProcessor.isValid` didn't check that the discarded card is in the hand (the deck's assert failed),
  and unboxed a null distance for a pilot's move to a Cylon Fleet space (`DISTANCE_LOOKUP_TABLE` has Galactica's
  spaces only).
- Fix: the discard has to be held (`holdsSkillCards`), and a move without a distance is rejected, as in
  `ActivateViperEventProcessor`.
- Tests: `MoveEventProcessorTest.shouldNotDiscardACardHeDoesNotHold` and `shouldNotFlyToTheCylonFleetBoard`,
  `ProbeTest.aDiscardOfACardNotInTheHandIsRejected` (flipped), journal `fuzz-3p-5.json` (F-2928df). F-86ead5's seed
  now plays into the NO_END under "Questions", so its journal was not kept.
- Files: `MoveEventProcessor`.

### F-d897ee - CRASH - fixed 2026-11-22

- Cause: "Dogfight" damaged an assault raptor in the "Reserves", which `ShipOps.damage` moved to the damaged vipers
  (a `ClassCastException` in `ShipZones`). In space it was already destroyed.
- Fix: a damaged assault raptor is destroyed in the "Reserves" too (rule 11.3.2.3).
- Tests: `DamageHumanFighterEventProcessorTest.shouldDestroyAnAssaultRaptorInTheReserves`, journal `fuzz-4p-4.json`.
- Files: `ShipOps`.

### F-4985f5 - CRASH - fixed 2026-11-22

- Cause: a `SendToBrigEvent` queued for a character who was in the "Brig" by the time it ran, on three paths: (1)
  `DrawMutinyCardEventProcessor.afterDrawing` built the "Brig" move and the Mutiny Card discards behind "Moral
  Dilemma"'s discard, and a "Bait" among Lee Adama's discards drew him another Mutiny Card that sent him there first
  (the "Press Room" had the same order); (2) "Personal Vices" drew the current player a second Mutiny Card in the
  check, then "Cylon Accusation"'s fail sent him there again; (3) "Consult the Prisoner"'s fail has everyone discard
  before it sends the current player, and his "Bait" drew him a second Mutiny Card in between.
- Fix: `SendToBrigEvent` leaves a character who can't be sent where he is (already in the "Brig", a revealed Cylon,
  rule 6.1.2.1). Rule 9.2.3 binds only a choice, so the five decisions that choose a character now take a
  `ChooseCharacterForBrigEvent` (in `FORMAT_VERSION` 38), offered only when someone can be chosen ("A Traitor Accused"
  asked even when nobody could be). And the "Brig" and the Mutiny Card discards come before "Moral Dilemma"
  (`choices.md`), so the discards a second card brings see the right hand.
- Tests: `SendToBrigEventProcessorTest.shouldLeaveACharacterAlreadyInTheBrigThere` and
  `shouldLeaveARevealedCylonWhereHeIs`, `ChooseCharacterForBrigEventProcessorTest`,
  `SkillCheckResultFollowupsTest.shouldNotAskForACharacterNobodyCouldBe`,
  `DrawMutinyCardEventProcessorTest.leeAdamaGoesToTheBrigBeforeHeDiscardsForTheSecondCard`, journals
  `fuzz-3p-1.json` (path 2) and `fuzz-6p-6.json` (path 3).
- Files: `SendToBrigEventProcessor`, `ChooseCharacterForBrigEvent`, `ChooseCharacterForBrigEventProcessor`,
  `CrisisCardFollowups`, `SkillCheckResultFollowups`, `DrawMutinyCardEventProcessor`, `PressRoomEventProcessor`,
  `choices.md`.

### F-8d5876 - CRASH - fixed 2026-11-22

- Cause: `AbilityTriggers.check` queued an offer for every noted trigger after every event. One event noted "Memento"
  and "Emotionally Fragile"; after the "Memento" offer, the check queued "Emotionally Fragile" again, and the second
  one ran after its trigger was cleared.
- Fix: the check queues one offer at a time; the check after it queues the next.
- Tests: `AbilityTriggersTest.twoTriggersAreOfferedOneAtATime`. No journal: the second offer is queued in it.
- Files: `AbilityTriggers`.

### F-481788 - CRASH - fixed 2026-11-22

- Cause: "A Guilty Verdict" executed a player, whose morale loss triggered "Emotionally Fragile"; Dee's execution ran
  before the first player chose his new character, and her Final Five card asked `isHuman()` of a player without a
  character (a `NullPointerException`).
- Fix: a player without a character isn't human (`Player.isHuman`), so no effect on the humans reaches him.
- Tests: `ExecuteEventProcessorTest.aPlayerWithoutACharacterDoesNotDiscardForAFinalFiveCard`,
  `PlayerTest.aPlayerWithoutACharacterIsNotHuman`, journal `fuzz-4p-8.json`.
- Files: `Player`.

### F-1195de - DEAD_OPTION - fuzzer, fixed 2026-11-22

- Cause: the fuzzer drew a list of ship ids from one place only, but "Authorization of Brutal Force" takes 3 raiders
  from any areas, or a centurion, which no list held.
- Fix: `Domains.shipGroups` adds a group per ship type in space and the Boarding Party track.
- Tests: `CandidatesTest.aListOfShipsTakesThemFromAnyArea` and `aListOfShipsTakesACenturion`.
- Files: `Domains`.

### F-9f4ada - CRASH - fixed 2026-11-22

- Cause: `SkillCheckOps.playFromDestinyDeck` drew both cards of a play at once from a Destiny deck that held one, and
  `createDestinyDeck` drew 2 of each color even when a color had fewer left.
- Fix: the play draws one card at a time and builds the deck anew as soon as it is empty, and the new deck takes what
  each color has left (rule 8.2.2.5).
- Tests: `PlayFromDestinyDeckEventProcessorTest.shouldBuildTheDeckAnewBetweenTheTwoCards`,
  `CreateDestinyDeckEventProcessorTest.leavesOutAColorWithNoCardsLeft`, journal `fuzz-7p-49.json`.
- Files: `SkillCheckOps`.

### F-b9a6d6 - CRASH - fixed 2026-11-22

- Cause: the CAG's "activate, then give" queued the activation's followups before the `MakeCagEvent`, so "Alert
  Viper Pilot"'s extra action came while he still held the title, and he passed it again; the first `MakeCagEvent`
  then named a character who already was CAG.
- Fix: the title passes before what the activation brings resolves (`choices.md`).
- Tests: `PassCagTitleEventProcessorTest.theCagActivatesAViperThenPassesTheTitle`. No journal: the stale event is
  queued in it.
- Files: `PassCagTitleEventProcessor`, `choices.md`.

### F-78b08a - DEAD_OPTION - fixed 2026-11-22

- Cause: "Review Camera Footage" checked whether the CAG could activate an unmanned viper when the card resolved,
  before its own "damage 2 vipers in the Reserves" ran, so he could be offered an activation without a viper left.
- Fix: `OfferUnmannedViperActivationEvent` builds the CAG's offer when it runs ("Training a Rookie" uses it too). New
  event: `FORMAT_VERSION` 39, and the fuzz journals' `formatVersion` went up with it (their entries are unchanged).
- Second path (10,000 games): "Command" queued both of its activations at once, on the comment's word that an
  activation leaves a viper to activate; but "Alert Viper Pilot" boards the viper an unmanned launch put in space.
  The second activation is an `OfferUnmannedViperActivationEvent` now.
- Tests: `OfferUnmannedViperActivationEventProcessorTest`, `CommandEventProcessorTest`. No journal: the offer is
  queued in it.
- Files: `OfferUnmannedViperActivationEvent`, `OfferUnmannedViperActivationEventProcessor`, `CrisisCardFollowups`,
  `CommandEventProcessor`.

### F-70a615 - NO_ANSWER - fixed 2026-11-22

- Cause: "Dutiful" queued its discard and the location's decision together. A "Bait" discarded for it drew Louis
  Hoshi a second Mutiny Card, which sent him to the "Brig", where the decision had no answer.
- Fix: `DutifulAgainEvent` offers the action after the discard, only while he can still take it.
- Tests: `DutifulAgainEventProcessorTest`, `DutifulEventProcessorTest`. No journal: the decision is queued in it.
- Files: `DutifulEventProcessor`, `DutifulAgainEvent`, `DutifulAgainEventProcessor`.

### F-4dc6dd - NO_ANSWER - fuzzer, fixed 2026-11-22

- Cause: the fuzzer's lists of Skill Cards had at most 4 cards, and "Impeachment" discards 5.
- Fix: `Domains` draws lists of up to 8 cards of the hand.
- Tests: `CandidatesTest.aListOfSkillCardsTakesFive`, journal `fuzz-3p-26.json` (`answers`).
- Files: `Domains`.

### F-7c5468, F-ae17df, F-8fabbb - CRASH, NO_ANSWER - fixed 2026-11-22

- Cause: four sections of Crisis Cards build their second part with the first: "Declare Martial Law" (-1 morale,
  then the Admiral becomes President), "Truth and Reconciliation" (-1 morale, then a character to the "Brig"),
  "Unwelcome Faces" (the Admiral discards his hand, then he sends a character to the "Brig") and "Dishonest Tactics"
  (-1 morale, then the President may move someone from the "Brig"). The morale loss executed "Emotionally Fragile"
  Dee, the Admiral, before the title came (F-7c5468, 16 games); a "Bait" in the Admiral's discard sent the last
  character who could go to the "Brig" there (F-ae17df); an execution replaced the prisoner an option named
  (F-8fabbb).
- Fix: `ResolveCrisisSectionThenEvent(card, section)` asks `CrisisCardFollowups.secondPart` for it once the first part
  is done. New event: `FORMAT_VERSION` 40.
- Tests: `ResolveCrisisSectionThenEventProcessorTest`. No journal: the second parts are queued in them.
- Files: `ResolveCrisisSectionThenEvent`, `ResolveCrisisSectionThenEventProcessor`, `CrisisCardFollowups`,
  `Disclosure`.

### F-704290 - CRASH - fixed 2026-11-22

- Cause: "Resolute" let Sharon "Athena" Agathon take the "Hangar Deck"'s action from wherever she was, and the
  deck's "1 more action" took it again while she was flying the viper it launched: she launched a second one and
  piloted two, so a later move found her on two boards.
- Fix: the "Hangar Deck" has no action for a pilot, and a character who flies a ship can't launch himself again.
- Tests: `HangarDeckEventProcessorTest.notForAPilot`, `LaunchViperEventProcessorTest.notAPilotWhoIsFlyingAlready`.
  No journal: its game is already broken where it starts.
- Files: `HangarDeckEventProcessor`, `LaunchViperEventProcessor`.

### F-040758 - CRASH - fixed 2026-11-22

- Cause: "A Guilty Verdict" executed Romo Lampkin, the Admiral, and then the Admiral's discard, built as a
  `DeceitfulEvent` for him, came to his new character, who has no "Deceitful".
- Fix: a `DeceitfulEvent` for a player who no longer has the ability is the discard in full.
- Tests: `DeceitfulEventProcessorTest.inFullOnceRomoLampkinIsGone` and `oncePerCard`.
- Files: `DeceitfulEventProcessor`.

### F-b894d2 - CRASH - fixed 2026-11-22

- Cause: the "Resurrection Ship" drew a Super Crisis Card when none was left in the deck or its discard pile.
- Fix: the location has no action then (`ResurrectionShipEventProcessor.canAct`).
- Tests: `ResurrectionShipEventProcessorTest.notWithoutASuperCrisisCardLeft`.
- Files: `ResurrectionShipEventProcessor`, `LocationEvent`.

### F-130933 - NO_ANSWER - fixed 2026-11-22

- Cause: "Unorthodox Plan" checked "Terminal Illness"'s 2 cards against a hand that still held the card itself, so
  Laura Roslin could play it with one other card and then not pay for the location.
- Fix: the location's cost is counted without the card and without "Reluctant"'s card.
- Tests: `UnorthodoxPlanEventProcessorTest.notWithoutTheCardsTheLocationCosts`.
- Files: `UnorthodoxPlanEventProcessor`.

### F-a3729e - DEAD_OPTION - fixed 2026-11-22

- Cause: "Return to Duty" offered every pilot on Galactica a launch at once; one launched the last viper of the
  "Reserves", and the next one's offer had no answer.
- Fix: `OfferPilotLaunchEvent` asks each in turn as the "Reserves" are then (in `FORMAT_VERSION` 40).
- Tests: `OfferPilotLaunchEventProcessorTest`, `CrisisCardFollowupsTest`. No journal: the offers are queued in it.
- Files: `OfferPilotLaunchEvent`, `OfferPilotLaunchEventProcessor`, `CrisisCardFollowups`.

### F-ad8366 - DEAD_OPTION - fixed 2026-11-22

- Cause: Sharon "Athena" Agathon, a Cylon Leader who wasn't Infiltrating, used "Resolute" on the "Basestar Bridge"
  to take the "Hangar Deck"'s action and launched herself in a viper. Shot down, she went to "Sickbay", a human
  location from which a Cylon player can only move to a Cylon location by discarding a card, and she had none: her
  Movement step offered a move without an answer.
- Fix: a Cylon player flies none of the humans' ships (the Cylon reveal sends his viper back to the "Reserves"): the
  "Hangar Deck" has no action for him and a piloted launch needs a human.
- Tests: `HangarDeckEventProcessorTest.notForACylonPlayer`, `LaunchViperEventProcessorTest.notACylonPlayer`. No
  journal: its state is already broken where it starts.
- Files: `HangarDeckEventProcessor`, `LaunchViperEventProcessor`.

### F-316031 - DEAD_OPTION - fuzzer, fixed 2026-11-22

- Cause: Lee "Apollo" Adama could reveal "Selfish" (a hand worth 20 strength), but its discard needed 10 cards, and
  the fuzzer drew lists of at most 8, at random.
- Fix: `Domains` also tries the hand's strongest cards first, every prefix of them.
- Tests: `CandidatesTest.aListOfSkillCardsReachesAStrength`, journal `fuzz-5p-9314.json` (`answers`).
- Files: `Domains`.

### F-f8b0c5, F-cb7417 - CRASH - fixed 2026-11-22

- Cause: in a "Brig" check, "Protect the Fleet" launched an unmanned assault raptor; "Alert Viper Pilot" let Lee
  "Apollo" Adama board it and take an action, an "Executive Order" whose actions started more "Brig" checks inside
  the first. The engine holds one active check, so the inner one's end cleared it, and the outer check's abilities
  loop found none (a `NullPointerException`).
- Fix: during a skill check "Alert Viper Pilot" boards the viper but grants no action (`choices.md`).
- Tests: `AlertViperPilotEventProcessorTest.noActionInASkillCheck`. No journal: the inner checks are in it.
- Files: `AlertViperPilotEventProcessor`, `choices.md`.

## Questions

- **F-e700fd** (NO_END, 275 of 50,000 games at 10,000 per player count, most of them with 3 players). Answered
  2026-11-22: not a bug. Every human in the "Brig" is a valid state: the characters there don't perform the Crisis
  step (rules 8.5 and 9.2), so hardly any Crisis Card is drawn, and random play rarely passes the "Brig"'s check to
  leave. Of the 22 NO_END games at 1,000 per player count, 16 ended with every human in the "Brig" and the other 6 had
  one human out who had been there most of the game.
