# Fuzzer report

Random games played by `./gradlew :core:fuzz` (see `fuzzer-plan.md`). The block between the markers is rewritten by
every run; "Fixes" and "Questions" are written by hand.

<!-- fuzz:begin -->

## Last run

2026-11-22 09:43, `b758639` (23 changed files), seeds 1-20, 3-7 players, 1s.

| Players | Games | Finished | Failed | Humans won | Cylons won | Rounds (median / max) |
|---|---|---|---|---|---|---|
| 3 | 20 | 12 | 8 | 0 | 12 | 13 / 44 |
| 4 | 20 | 15 | 5 | 0 | 15 | 6 / 24 |
| 5 | 20 | 12 | 8 | 0 | 12 | 4 / 10 |
| 6 | 20 | 14 | 6 | 0 | 14 | 3 / 7 |
| 7 | 20 | 9 | 11 | 0 | 9 | 3 / 5 |
| All | 100 | 62 | 38 | 0 | 62 | 5 / 44 |

### Endings

| Ending | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|
| Fuel at 0 | 1 | 1 | 0 | 4 | 2 |
| Food at 0 | 0 | 0 | 0 | 1 | 0 |
| Morale at 0 | 7 | 3 | 9 | 3 | 1 |
| Population at 0 | 3 | 5 | 2 | 5 | 3 |
| 6 damaged Galactica locations | 0 | 1 | 0 | 1 | 0 |
| Centurions at the end of the Boarding Party track | 1 | 5 | 0 | 0 | 2 |
| No character left to choose | 0 | 0 | 0 | 0 | 0 |
| Kobol, the humans win | 0 | 0 | 0 | 0 | 0 |
| Kobol, a resource at 0 after the Personal Goals | 0 | 0 | 1 | 0 | 1 |

### Findings

| ID | Kind | What | Games | Example | Status |
|---|---|---|---|---|---|
| F-7c295f | DEAD_OPTION | An option leads to `DeclareEmergencyEvent`, which has no legal answer | 100 | 3p seed 1 | open |
| F-ea9336 | INPUT_CRASH | `NullPointerException` in `DeclareEmergencyEventProcessor.isValid` on `DeclareEmergencyEvent` | 100 | 3p seed 1 | open |
| F-a6e77d | DEAD_OPTION | An option leads to `ChangeOfPlansEvent`, which has no legal answer | 74 | 3p seed 1 | open |
| F-d95b59 | INPUT_CRASH | `NullPointerException` in `ChangeOfPlansEventProcessor.isValid` on `ChangeOfPlansEvent` | 74 | 3p seed 1 | open |
| F-ca1339 | DEAD_OPTION | An option leads to `PoliticalProwessEvent`, which has no legal answer | 64 | 3p seed 1 | open |
| F-db09a4 | INPUT_CRASH | `NullPointerException` in `PoliticalProwessEventProcessor.isValid` on `PoliticalProwessEvent` | 64 | 3p seed 1 | open |
| F-369710 | INPUT_CRASH | `NullPointerException` in `Discard1MutinyCardEventProcessor.isValid` on `Discard1MutinyCardEvent` | 40 | 3p seed 1 | open |
| F-896a90 | CRASH | `IndexOutOfBoundsException` in `BaitEventProcessor.process` on `ResolveSkillCheckAbilityEvent` | 11 | 3p seed 11 | open |
| F-2928df | CRASH | `AssertionError` in `MoveEventProcessor.process` on `MoveEvent` | 10 | 3p seed 5 | open |
| F-86ead5 | INPUT_CRASH | `NullPointerException` in `MoveEventProcessor.validSpaceDistance` on `MoveEvent` | 8 | 3p seed 14 | open |
| F-d897ee | CRASH | `ClassCastException` in `DamageHumanFighterEventProcessor.process` on `DogfightEvent` | 5 | 4p seed 4 | open |
| F-dafda6 | CRASH | `IndexOutOfBoundsException` in `BaitEventProcessor.process` on `Discard1MutinyCardEvent` | 2 | 3p seed 4 | open |
| F-0634f1 | CRASH | `IndexOutOfBoundsException` in `BaitEventProcessor.process` on `PlayCardsIntoSkillCheckEvent` | 2 | 4p seed 1 | open |
| F-4985f5 | CRASH | The queued `SendToBrigEvent` is invalid (after `Discard1MutinyCardEvent`) | 2 | 5p seed 15 | open |
| F-1195de | DEAD_OPTION | An option leads to `AuthorizationOfBrutalForceEvent`, which has no legal answer | 1 | 3p seed 7 | open |
| F-481788 | CRASH | `NullPointerException` in `ExecuteEventProcessor.finalFiveRevealed` on `ChooseCrisisSectionEvent` | 1 | 4p seed 8 | open |
| F-1c6b6e | INPUT_CRASH | `NullPointerException` in `DiscardDownTo1MutinyCardEventProcessor.isValid` on `DiscardDownTo1MutinyCardEvent` | 1 | 4p seed 14 | open |
| F-7f36f4 | CRASH | `IndexOutOfBoundsException` in `BaitEventProcessor.process` on `DogfightEvent` | 1 | 6p seed 5 | open |
| F-f39319 | INPUT_CRASH | `NullPointerException` in `TiebreakCardsBeforeSkillCheckEventProcessor.isValid` on `TiebreakCardsBeforeSkillCheckEvent` | 1 | 6p seed 12 | open |
| F-5eb977 | CRASH | `IndexOutOfBoundsException` in `DrawAndResolveRecklessCardsEventProcessor.process` on `ResolveSkillCheckAbilityEvent` | 1 | 7p seed 3 | open |
| F-104b07 | CRASH | `IndexOutOfBoundsException` in `DrawAndResolveRecklessCardsEventProcessor.process` on `Discard1MutinyCardEvent` | 1 | 7p seed 4 | open |
| F-8d5876 | CRASH | The queued `EmotionallyFragileEvent` is invalid (after `NoOpEvent`) | 1 | 7p seed 8 | open |
| F-17bf33 | CRASH | `IndexOutOfBoundsException` in `ABetterMachineEventProcessor.process` on `ResolveSkillCheckAbilityEvent` | 1 | 7p seed 14 | open |

- **F-7c295f**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=1`, trace `core/build/fuzz/F-7c295f-3p-1.txt`. Turn 2, `CRISIS`, player 1 chooses among 2 options.
- **F-ea9336**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=1`, trace `core/build/fuzz/F-ea9336-3p-1.txt`. Turn 2, `CRISIS`, player 1 chooses among 2 options.
- **F-a6e77d**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=1`, trace `core/build/fuzz/F-a6e77d-3p-1.txt`. Turn 44, `ACTION`, player 1 chooses among 2 options.
- **F-d95b59**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=1`, trace `core/build/fuzz/F-d95b59-3p-1.txt`. Turn 44, `ACTION`, player 1 chooses among 2 options.
- **F-ca1339**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=1`, trace `core/build/fuzz/F-ca1339-3p-1.txt`. Turn 12, `ACTION`, player 1 chooses among 2 options.
- **F-db09a4**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=1`, trace `core/build/fuzz/F-db09a4-3p-1.txt`. Turn 12, `ACTION`, player 1 chooses among 2 options.
- **F-369710**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=1`, trace `core/build/fuzz/F-369710-3p-1.txt`. Turn 9, `CRISIS`, player 1 asked for `Discard1MutinyCardEvent`.
- **F-896a90**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=11`, trace `core/build/fuzz/F-896a90-3p-11.txt`. Turn 25, `CRISIS`, player 2 chooses among 3 options.
- **F-2928df**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=5`, trace `core/build/fuzz/F-2928df-3p-5.txt`. Turn 7, `MOVEMENT`, player 2 asked for `MoveEvent`.
- **F-86ead5**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=14`, trace `core/build/fuzz/F-86ead5-3p-14.txt`. Turn 1, `MOVEMENT`, player 2 chooses among 2 options.
- **F-d897ee**: `./gradlew :core:fuzz -Pplayers=4 -Pseed=4`, trace `core/build/fuzz/F-d897ee-4p-4.txt`. Turn 4, `CRISIS`, player 3 asked for `DogfightEvent`.
- **F-dafda6**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=4`, trace `core/build/fuzz/F-dafda6-3p-4.txt`. Turn 39, `MOVEMENT`, player 3 asked for `Discard1MutinyCardEvent`.
- **F-0634f1**: `./gradlew :core:fuzz -Pplayers=4 -Pseed=1`, trace `core/build/fuzz/F-0634f1-4p-1.txt`. Turn 26, `CRISIS`, player 1 asked for `PlayCardsIntoSkillCheckEvent`.
- **F-4985f5**: `./gradlew :core:fuzz -Pplayers=5 -Pseed=15`, trace `core/build/fuzz/F-4985f5-5p-15.txt`. Turn 6, `CRISIS`, player 1 asked for `Discard1MutinyCardEvent`.
- **F-1195de**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=7`, trace `core/build/fuzz/F-1195de-3p-7.txt`. Turn 30, `ACTION`, player 3 chooses among 2 options.
- **F-481788**: `./gradlew :core:fuzz -Pplayers=4 -Pseed=8`, trace `core/build/fuzz/F-481788-4p-8.txt`. Turn 47, `CRISIS`, player 4 chooses among 2 options.
- **F-1c6b6e**: `./gradlew :core:fuzz -Pplayers=4 -Pseed=14`, trace `core/build/fuzz/F-1c6b6e-4p-14.txt`. Turn 2, `ACTION`, player 4 asked for `DiscardDownTo1MutinyCardEvent`.
- **F-7f36f4**: `./gradlew :core:fuzz -Pplayers=6 -Pseed=5`, trace `core/build/fuzz/F-7f36f4-6p-5.txt`. Turn 13, `CRISIS`, player 5 asked for `DogfightEvent`.
- **F-f39319**: `./gradlew :core:fuzz -Pplayers=6 -Pseed=12`, trace `core/build/fuzz/F-f39319-6p-12.txt`. Turn 4, `CRISIS`, player 4 asked for `TiebreakCardsBeforeSkillCheckEvent`.
- **F-5eb977**: `./gradlew :core:fuzz -Pplayers=7 -Pseed=3`, trace `core/build/fuzz/F-5eb977-7p-3.txt`. Turn 24, `CRISIS`, player 2 chooses among 2 options.
- **F-104b07**: `./gradlew :core:fuzz -Pplayers=7 -Pseed=4`, trace `core/build/fuzz/F-104b07-7p-4.txt`. Turn 42, `CRISIS`, player 1 asked for `Discard1MutinyCardEvent`.
- **F-8d5876**: `./gradlew :core:fuzz -Pplayers=7 -Pseed=8`, trace `core/build/fuzz/F-8d5876-7p-8.txt`. Turn 35, `ACTIVATE_CYLON_SHIPS`, player 1 chooses among 2 options.
- **F-17bf33**: `./gradlew :core:fuzz -Pplayers=7 -Pseed=14`, trace `core/build/fuzz/F-17bf33-7p-14.txt`. Turn 23, `CRISIS`, player 3 chooses among 2 options.

### Watch

- 3 players: the humans never won in 20 games.
- 3 players: no Kobol ending in 20 games.
- 4 players: the humans never won in 20 games.
- 4 players: no Kobol ending in 20 games.
- 5 players: the humans never won in 20 games.
- 6 players: the humans never won in 20 games.
- 6 players: no Kobol ending in 20 games.
- 7 players: the humans never won in 20 games.
- No "No character left to choose" ending in 100 games.

<details><summary>Event types that never ran (33)</summary>

`AdaptableEvent`, `AnalyzeEnemyFighterPassEvent`, `AssignChiefOfStaffEvent`, `AssumeCommandEvent`, `BroadcastEvent`, `CivilianSelfDefenseEvent`, `CivilianSelfDefenseResultEvent`, `ConsultTheHybridPassEvent`, `CylonGenocidePassEvent`, `CylonPowerSendToSickbayEvent`, `DefendPresidencyEvent`, `DiscardQuorumCardsEvent`, `EndGameEvent`, `EulogyEvent`, `HumanPrisonerFailEvent`, `HumanPrisonerTakeHandEvent`, `ImpeachmentEvent`, `IntolerantEvent`, `KleptomaniaBrigEvent`, `LaunchReservesEvent`, `MaximumFirepowerEvent`, `MysteriousMessagePassEvent`, `OfferAlertViperPilotEvent`, `PresidentialOrderEvent`, `PresidentialPardonEvent`, `ResignationEvent`, `ResolveFinalFiveCardsEvent`, `ResourcesForGalacticaEvent`, `SacrificeEvent`, `SetATrapPassEvent`, `StandoffWithPegasusPassEvent`, `UseArbitratorEvent`, `UseChiefOfStaffEvent`

</details>

### History

| Run | Commit | Games | Failed | Open findings |
|---|---|---|---|---|
| 2026-11-22 09:43 | `b758639` | 100 | 38 | 23 |
| 2026-11-22 09:37 | `b758639` | 100 | 38 | 15 |
| 2026-11-22 09:37 | `b758639` | 100 | 38 | 16 |

<!-- fuzz:end -->

## Fixes

## Questions
