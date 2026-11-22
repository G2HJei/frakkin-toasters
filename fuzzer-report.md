# Fuzzer report

Random games played by `./gradlew :core:fuzz` (see `fuzzer-plan.md`). The block between the markers is rewritten by
every run; "Fixes" and "Questions" are written by hand.

<!-- fuzz:begin -->

## Last run

2026-11-22 13:08, `d17eb9a` (35 changed files), seeds 1-100, 3-7 players, 4s.

| Players | Games | Finished | Failed | Humans won | Cylons won | Rounds (median / max) |
|---|---|---|---|---|---|---|
| 3 | 100 | 98 | 2 | 0 | 98 | 9 / 49 |
| 4 | 100 | 100 | 0 | 0 | 100 | 7 / 50 |
| 5 | 100 | 100 | 0 | 0 | 100 | 5 / 27 |
| 6 | 100 | 100 | 0 | 0 | 100 | 4 / 28 |
| 7 | 100 | 100 | 0 | 0 | 100 | 3 / 14 |
| All | 500 | 498 | 2 | 0 | 498 | 5 / 50 |

### Endings

| Ending | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|
| Fuel at 0 | 11 | 13 | 11 | 11 | 8 |
| Food at 0 | 4 | 4 | 3 | 2 | 5 |
| Morale at 0 | 38 | 44 | 44 | 39 | 48 |
| Population at 0 | 32 | 25 | 28 | 33 | 27 |
| 6 damaged Galactica locations | 3 | 4 | 0 | 1 | 2 |
| Centurions at the end of the Boarding Party track | 7 | 7 | 9 | 10 | 9 |
| No character left to choose | 0 | 0 | 0 | 0 | 0 |
| Kobol, the humans win | 0 | 0 | 0 | 0 | 0 |
| Kobol, a resource at 0 after the Personal Goals | 3 | 3 | 5 | 4 | 1 |

### Findings

| ID | Kind | What | Games | Example | Status |
|---|---|---|---|---|---|
| F-e700fd | NO_END | No ending after 50 rounds | 2 | 3p seed 58 | question |

- **F-e700fd**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=58`, trace `core/build/fuzz/F-e700fd-3p-58.txt`. Turn 153, `RECEIVE_SKILLS`, player 2 asked for `DrawSkillCardsEvent [DRAW_EXACTLY_5]`.

### Watch

- 3 players: the humans never won in 100 games.
- 4 players: the humans never won in 100 games.
- 5 players: the humans never won in 100 games.
- 6 players: the humans never won in 100 games.
- 7 players: the humans never won in 100 games.
- No "No character left to choose" ending in 500 games.

<details><summary>Event types that never ran (8)</summary>

`BroadcastEvent`, `DefendPresidencyEvent`, `DiscardQuorumCardsEvent`, `EndGameEvent`, `IntolerantEvent`, `MysteriousMessagePassEvent`, `ResolveFinalFiveCardsEvent`, `UseArbitratorEvent`

</details>

### History

| Run | Commit | Games | Failed | Open findings |
|---|---|---|---|---|
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
- Tests: `OfferUnmannedViperActivationEventProcessorTest`. No journal: the offer is queued in it.
- Files: `OfferUnmannedViperActivationEvent`, `OfferUnmannedViperActivationEventProcessor`, `CrisisCardFollowups`.

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

## Questions

- **F-e700fd** (NO_END, 3p seed 14): every character was in the "Brig" by turn 25, and the characters in the "Brig"
  don't perform the Crisis step (rules 8.5 and 9.2), so in 128 more turns only 2 Crisis Cards were drawn and the game
  passed the 50 rounds. The engine follows the rules: the players are offered the "Brig"'s action (12 tries in this
  game, a difficulty 7 check that the revealed Cylon plays against), and random play rarely gets out. Options: (a) a
  smarter agent in the "Brig" (against decision 2, uniform random play); (b) a non-fatal kind for a stalled game, e.g.
  every human in the "Brig" for 10 rounds, so it is counted but doesn't fail the exit; (c) keep it fatal and raise the
  cap. Recommendation: (b), as the rules allow the stall and only a player's choice ends it.
