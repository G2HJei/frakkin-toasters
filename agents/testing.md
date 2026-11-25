# Testing

JUnit 5 + Mockito. A test sets up game state, executes the event, and asserts the state changes and followups. Use
real game ops in processor tests, not mocks, since processors may read their own changes.

## Harnesses and doubles

- `TestHarness`: base class that builds a `Game` with fake components and exposes the boards, decks, ships, the
  `journal` and `recorder`, and an `ops` instance bound to them. Game op tests (`operation/`, e.g.
  `PlayerOpsDrawSkillCardTest`) extend it directly, and also assert the logged facts (`journal.facts()`).
  `FactApplierTest` covers each fact.
- `EventTestHarness<E>`: base class for processor tests. Name the test `XxxEventProcessorTest` in the processor's
  package and the harness creates the processor by reflection. Use `execute(event)`, `assertFollowup(...)`,
  `assertInvalid(event)`, `assertSkillCheckTriggered(type)`. A test that asserts no followup is checked for
  `Followup.NONE`. Players start without a character: a location action test gives one and `moveTo`s the location
  first. A character can be set only once.
- `FakeDie`: `nextRoll(int)` controls the die. A result event test calls `rolled(int)` first, which starts and rolls a
  `DieRoll` the way the window and `RollDieEvent` do.
- `FakeDeck<T>`: `nextCard(T...)` controls draws, `clear()` empties the draw pile.
- Engine tests use a seeded `Randomness`, not `FakeDeck`, because restore rebuilds the real decks.

## Test fixtures

`core` has test fixtures (`src/testFixtures`, plugin `java-test-fixtures`) for what `backend` tests use too, which
`ArchitectureTest` leaves out like the tests:

- the test-only events with their processors and `TestOps` (in the fixtures' `operation` package, because only the ops
  can record a fact);
- `GameSnapshot` (`engine/journal/`): a `Game` as comparable plain values; replay tests compare a live game with its
  replayed twin;
- `ScriptedScenario.play(journal)` records every kind of fact, which `GameReplayerTest` checks (one of its scopes has no
  event, so only `GameReplayer` rebuilds it, not `GameEngine.restore`);
- `ScriptedTurns`: `TurnLoopTest`'s seeded game as a list of inputs, each computed from the game when it's its turn, for
  tests that drive a game from outside (the codec, the sessions, the web socket). Starbuck lets each revealed Crisis
  Card resolve ("Secret Destiny") and Laura Roslin picks from her 2 ("Religious Visions"), so 5 cards are stacked;
- `TestEngines` (starts an engine with any first event) and the random playouts (`fuzz/`, below).

Test-only setup events: `ScriptEvent` (calls `ops.startGame()` and queues a script), `FailingEvent` (changes the game,
then throws: rollback), `AnytimeTestEvent` (spends its player's miracle token, runs ahead of decisions, choices and
secret inputs), `StackedStartGameEvent` (starts like `StartGameEvent` with simple Crisis Cards on top: only the section
to ask, no die rolls), `GiveSkillCardEvent`, `StackDestinyDeckEvent`, `StackCrisisCardEvent`, `SetUpGalacticaEvent`,
`PlaceRaiderEvent`, `StackDeckEvent`, `DrawMotiveCardsEvent`. The registry finds test processors such as `ScriptEventProcessor` too, because
it scans the whole classpath. The sandbox events (`GiveQuorumCardToPresidentEvent`, `GiveMutinyCardToPlayerEvent`,
`GiveSuperCrisisCardToPlayerEvent`, `GiveLoyaltyCardToPlayerEvent`) deal cards in tests as well.

## Engine tests

Engine tests play through `submit` and `choose` and restore at every pause: `restore` has to give the same
`GameSnapshot` and statuses. A test script that plays to the end has to end the game, e.g. with a `CylonsWinEvent`.

| Test | What it plays |
|---|---|
| `GameEngineTest` | a script of real events started by `ScriptEvent`; rollback (`FailingEvent`), anytime input (`AnytimeTestEvent`) |
| `TurnLoopTest` | a seeded game through several full turns (`StackedStartGameEvent`), inputs relative to the first player the seed picks |
| `LocationSkillCheckTest` | the "Admiral's Quarters", "Brig" and "Airlock" checks from the action to `EndSkillCheckEvent`; cards dealt (`GiveSkillCardEvent`, `StackDestinyDeckEvent`), each check asserts its total strength |
| `CrisisStepTest` | the Crisis step to the end of Activate Cylon Ships: a card's check or alternate instruction, "Fulfiller of Prophecy", "Secret Destiny" |
| `CrisisAbilitiesTest`, `SkillCheckAbilitiesTest` | the abilities that act on Crisis Cards, and in a skill check (from the location action to the end of the check) |
| `CharacterTriggersTest`, `CharacterPenaltiesTest` | triggers that answer something ("Memento", "For Love", "Necessary Steps", "Adaptable", "Manipulative"); penalties, bans and costs |
| `CrisisCardResolutionTest`, `SuperCrisisCardResolutionTest` | every section (and result) of every card through the real processors with a minimal engine, so none ends in an event without a processor |
| `InterruptWindowTest` | the secret status, a pass, a tiebreak, an "Evasive Maneuvers" reroll |
| `RollAbilitiesTest` | a reroll, a card and an ability from one player in a secret window, "Longshot", "FTL Operator" on a scripted "FTL Control" roll, "Hotshot" in a real Action step |
| `SkillCardActionsTest` | Skill Cards for their "Action:" and "Movement:", a script starting at the step that offers them |
| `QuorumCardActionsTest`, `MutinyCardActionsTest` | Quorum and Mutiny Card plays |
| `CharacterActionsTest` | "Action:" abilities ("Organized"'s three locations, "Skilled Politician", "Abuse Power") |
| `CharacterMovementTest` | "Movement:" abilities and the ones that act on moving or at the end of a step, in 4-player games (a Cylon Leader needs more than 3) |
| `SuperCrisisCardTest` | a revealed Cylon plays Super Crisis Cards at "Caprica" |
| `TitleCardActionsTest` | the nuke, the CAG's two actions |
| `CylonRevealTest` | a power, the discards before it, a reveal on another player's turn, the execution's choice |
| `CylonLeaderTest`, `FinalFiveTest`, `PegasusDestructionTest`, `SevenPlayerSetupTest` | the return to the "Resurrection Ship", the Final Five, Pegasus' destruction, a 7 player start with its Cylon Leader |
| `MotiveCardRevealTest` | a Motive Card revealed through `submit` while another player decides and while he chooses |
| `PersonalGoalTest` | "Self Destruction" with the new Loyalty Card, "Selfish" with its discards |
| `CharacterSetupTest` | a seeded start with Tom Zarek (alt.) and Samuel T. Anders: the Mutiny Card "Disreputable" starts with, the Receive Skills step "Starts on the Bench" skips |
| `JumpAndKobolTest` | four jumps and the Kobol end game: the Sleeper Agent phase, a human win, a Personal Goal that turns it into a Cylon win |

One-event-per-card checks: `SkillCardEventsTest`, `QuorumCardEventsTest`, `MutinyCardEventsTest`,
`CharacterActionEventsTest`, `CharacterMovementEventsTest`.

## Random playouts

`fuzz/` in the test fixtures; run with `./gradlew :core:fuzz` (`-Pgames`, `-Pplayers`, `-Pseed`, ...).

- `Playout.play(players, seed)` plays a seeded game with random legal answers until it ends, a finding stops it or it
  runs past a cap, and restores it on the way (the twins have to stay equal).
- `AnswerFinder` answers from the game's `LegalAnswers` ([Legal answers](engine.md#legal-answers)): a random class among
  them, then a random answer of it, a shape filled at random and judged again (its example when no filling fits). Empty
  answers are a `NO_ANSWER`, or a `DEAD_OPTION` for a choice (it looks ahead at the decision an option leads to).
- At 1 in 20 decisions a `Probe` first submits a hostile candidate (`Candidates.hostile`, from the wide domains of
  `Domains.wide`: any card of the decks, players 0 to n + 1, unknown ship ids, nulls), which the engine has to reject
  leaving the game as it was (`Twins`), or take, and then the answers have to cover it. At 1 in 10 answers `Mutations`
  sets one component to each of its wide values: an input the game takes that the answers don't cover is a
  `HINT_MISSING`.
- A `Finding` (`CRASH`, `INPUT_CRASH`, `NO_ANSWER`, `DEAD_OPTION`, `HINT_MISSING`, `RESTORE`, `NO_END`, `BAD_ENDING`,
  `REJECTED_BUT_CHANGED`, `FUZZER`) has an ID from a signature without line numbers, and its `Pause` (where, the pending
  input, the inputs before it, and with `Options.withRepro` the `Repro`: the journal there and the input that failed).
  `Endings.classify` reads the rule's ending from the final state.
- `FuzzRun` plays the seeds on a thread pool and `FuzzReport` rewrites the block of `fuzzer-report.md` between its
  markers (statuses from the hand-written "Fixes" and "Questions"), with a trace per finding in `core/build/fuzz`. A
  finding becomes a regression journal ([Backend](backend.md), "Regression journals").
- `AnswerFinderTest`, `CandidatesTest`, `EndingsTest`, `PlayoutTest`, `ProbeTest` and `FuzzReportTest` cover it without
  failing on what the engine does wrong.
