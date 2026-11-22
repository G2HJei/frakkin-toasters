# Fuzzer report

Random games played by `./gradlew :core:fuzz`. The block between the markers is rewritten by every run;
"Fixes" and "Questions" are written by hand.

<!-- fuzz:begin -->

## Last run

2026-11-22 18:28, `78a0421` (49 changed files), seeds 1-10000, 3-7 players, 14m 48s.

| Players | Games | Finished | Failed | Humans won | Cylons won | Rounds (median / max) |
|---|---|---|---|---|---|---|
| 3 | 10000 | 9686 | 314 | 3 | 9683 | 9 / 50 |
| 4 | 10000 | 9982 | 18 | 3 | 9979 | 7 / 50 |
| 5 | 10000 | 10000 | 0 | 3 | 9997 | 5 / 47 |
| 6 | 10000 | 10000 | 0 | 3 | 9997 | 4 / 36 |
| 7 | 10000 | 10000 | 0 | 2 | 9998 | 3 / 29 |
| All | 50000 | 49668 | 332 | 14 | 49654 | 5 / 50 |

### Endings

| Ending | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|
| Fuel at 0 | 1215 | 1190 | 1319 | 1304 | 1428 |
| Food at 0 | 576 | 539 | 567 | 502 | 504 |
| Morale at 0 | 4316 | 4087 | 4075 | 4109 | 3960 |
| Population at 0 | 2171 | 2555 | 2548 | 2563 | 2600 |
| 6 damaged Galactica locations | 255 | 318 | 291 | 282 | 299 |
| Centurions at the end of the Boarding Party track | 932 | 1101 | 967 | 1001 | 956 |
| No character left to choose | 0 | 0 | 0 | 0 | 0 |
| Kobol, the humans win | 3 | 3 | 3 | 3 | 2 |
| Kobol, a resource at 0 after the Personal Goals | 218 | 189 | 230 | 236 | 251 |

### Findings

| ID | Kind | What | Games | Example | Status |
|---|---|---|---|---|---|
| F-e700fd | NO_END | No ending after 50 rounds | 332 | 3p seed 36 | question |

- **F-e700fd**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=36`, trace `core/build/fuzz/F-e700fd-3p-36.txt`. Turn 153, `RECEIVE_SKILLS`, player 3 asked for `DrawSkillCardsEvent [DRAW_EXACTLY_5]`.

### Watch

- No "No character left to choose" ending in 50000 games.

<details><summary>Event types that never ran (1)</summary>

`DiscardQuorumCardsEvent`

</details>

### History

| Run | Commit | Games | Failed | Open findings |
|---|---|---|---|---|
| 2026-11-22 18:28 | `78a0421` | 50000 | 332 | 0 |
| 2026-11-22 18:07 | `78a0421` | 50000 | 334 | 4 |
| 2026-11-22 17:56 | `78a0421` | 5000 | 28 | 1 |
| 2026-11-22 17:56 | `78a0421` | 100 | 0 | 1 |

<!-- fuzz:end -->

## Fixes

Milestone B (`legal-answers-plan.md`): the fuzzer answers from the game's `LegalAnswers`, and a probe or a mutation the
game takes outside them is a `HINT_MISSING`.

- **F-9dd6f5** (HINT_MISSING): a draw that names a color at 0 draws nothing of it, so `covers` compares the counts per
  color without the zeros (`LegalAnswersTest`).
- **F-5945fa** (NO_ANSWER): a pick took at most 20 elements, and "Command Authority" left a hand of more than 30 cards
  to discard down from. A pick takes every element now (`LegalAnswersTest.aDiscardOfMoreThanTwentyCardsIsAPickToo`).
- **F-3bdf77** (CRASH): the result of a viper's attack is queued before the roll, and "Hotshot"'s discard of a
  Treachery card drew a second Mutiny Card that sent the pilot to the "Brig": the result now misses when its fighter or
  its target is gone (`AttackCylonShipResultEventProcessorTest`, `fuzz-3p-5505.json`).
- **F-c3d2d0** (CRASH): "Assume Command"'s discard of a "Bait" drew a second Mutiny Card that sent its player to the
  "Brig" before `MakeAdmiralEvent` ran: the event now gives the title only to a character who can hold it when it runs
  (`MakeAdmiralEventProcessorTest`, `fuzz-6p-8373.json`).

## Questions

- **F-e700fd** (NO_END): answered 2026-11-22, every human in the "Brig" is a valid state: nobody there draws a Crisis
  Card. Of the 329 NO_END games at 10,000 seeds per player count, 278 were that, and 51 (48 with 3 players, 3 with 4) a
  game where a human gets out of the "Brig" now and then: each ended on its own in 51 to 82 rounds with a cap of 300.
