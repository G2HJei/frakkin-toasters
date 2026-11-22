# Fuzzer plan (roadmap milestone A)

Status (2026-11-22): phases 1 to 4 are done; the first run (`-Pgames=20`, with the probes) is in `fuzzer-report.md`:
100 games, 38 failed, 23 open findings. Where phase 1 differs from the plan:

- `PlayInterruptCardEvent`, `UseRollAbilityEvent` and `ChooseCrisisTargetEvent` have no source of their own: the
  reflective one covers them with narrowed domains (`calculationsModifier` ±1, `result` 1 to 8, `UseRollAbilityEvent`'s
  ability from `RollAbilities.ABILITIES`, a `CrisisCard` from the turn's).
- A choice checks only the option it picked (and picks again without it when its decision has no answer), so a
  DEAD_OPTION shows up over many games, not in every one.
- `submit` rejecting what `prepare` accepted stops the game, although its kind (`FUZZER`) is not fatal otherwise.
- A queued event the engine refuses ("The queued X is invalid") is named by that event, not by the input before it.
- The twins come at an input drawn from [1, 54), [54, 270) and [270, 1134).
- `CandidatesTest` tests the tiebreak on `Combinations.oneOfEach` and "Calculations" on its domain, not on game states.

Where phases 2 to 4 differ:

- The fuzz JVM and the backend's test JVM run with `-XX:-OmitStackTraceInFastThrow`: the first run showed a hot
  `NullPointerException` losing its stack, so the same bug got a second ID (frame `?`).
- A trace is written for each finding's example game only (the lowest player count, then seed), from the pause where
  the game first showed it, and a batch run deletes the old traces first. A single-seed run also writes
  `<n>p-<seed>.txt`, the whole game. A run keeps the other games slim (`RunResults`), so 10,000 seeds fit in memory.
- The report's "Rounds" are of the finished games; "Open findings" in the history counts `open` and `regressed`. "Watch"
  lists per player count a team that never won and no Kobol ending, and over the run every other ending that never
  happened. `:core:fuzz` never fails on a finding; the report says what is open.
- The twins come at the first input at or past their place, so an accepted probe doesn't skip one.
- `Repro` (in `fuzz/`) holds the journal, the seed, the input and the expectation, and its `check()` is what
  `RegressionJournalsTest` runs; `RegressionJournals.repro` and `write` read and write it. The input may hold an
  `option` and an `event`: a candidate that crashed while a choice looked ahead at its decision is given after that
  option. A third expectation, `restores`, is for a twin that differed: after the input, the journal restores to the
  same game (it compares a restored game with a restore of it, so a difference only a game played live from the start
  has isn't caught). A rejected input that changed the game fails `runs` too, which covers REJECTED_BUT_CHANGED.
- `FuzzJournalExportTest` takes the stopping finding, else the game's first, or the one named by
  `-DfuzzJournal=4:117:F-1c2d3e`, and fails unless the written journal reproduces it. Checked on `F-d897ee` (4p seed 1)
  and on a probe's `F-2928df` (3p seed 5): `RegressionJournalsTest` failed on each, and both journals were deleted
  again, as nothing is fixed yet.
- A probe takes one class of the decision at random (no gate) and one candidate of it. Its `playerNumber` stays the
  asked player's, because the engine rejects any other before a processor sees it. Rejected probes are in the trace as
  `probe` lines. A probe's CRASH is told from an INPUT_CRASH by whether the engine took the input (a rollback replaces
  the journal, an `Error` leaves its scope behind), and a check that threw and changed the game is REJECTED_BUT_CHANGED.
- `PlayoutTest`'s two rounds run without probes, so an engine bug they hit can't end the game early; a test of its own
  checks that a seed probes the same way twice.

Implements milestone A of `roadmap.md`, "Prove the game is completable": a random-playout fuzzer that plays seeded
games with 3 to 7 players to an ending, answering every pause with a random legal answer, and a loop that runs it,
fixes what it finds and records every finding and fix in `fuzzer-report.md`.

Today nothing shows that a whole game can be played: the tests cover one feature each and a few scripted turns. A
deadlock (a decision nobody can answer), a rollback, a restore that gives another game, or a game that never ends
would first show up for real players.

Every phase ends with the tests green. The plan is done when:

- `./gradlew :core:fuzz` plays any number of seeds per player count in parallel and writes `fuzzer-report.md`;
- every finding can be reproduced from its seed with one command, and an engine bug becomes a regression journal
  with the input that failed;
- 10,000 seeds per player count finish without a fatal finding (the roadmap's exit), every fix is in the report's
  "Fixes" section and every open design call is in its "Questions" section.

## Target design

```text
./gradlew :core:fuzz -Pgames=100 -Pplayers=3-7        (or -Pplayers=4 -Pseed=117: one game, a full trace)
   |
   v
FuzzRun          warms up EventCatalog and ProcessorRegistry, then one game per task on a thread pool
   |
   v
Playout.play(players, seed)
   engine = GameEngine.start(players, seed); agent = new Random(mix(seed))
   until Finished, a fatal finding or a cap:
     AwaitingChoice   -> options sorted by toString(); an option that is a decision is kept only when
                         AnswerFinder finds an answer for it (otherwise DEAD_OPTION); pick one at random
     AwaitingDecision -> AnswerFinder.find(view, decision); none: NO_ANSWER
     sometimes        -> a legal RevealMotiveCardEvent (the only AnytimeEvent), a hostile probe (phase 4)
     at 3 random pauses: twin = GameEngine.restore(journal, seed), the same input to both, compare
   finished: Endings.classify(view) has to explain the outcome
   -> PlayoutResult(players, seed, ending, winner, rounds, inputs, findings, last inputs)
   |
   v
FuzzReport       rewrites the generated block of fuzzer-report.md, keeps everything else
                 a trace per finding in core/build/fuzz/<id>-<n>p-<seed>.txt
```

Everything new is in `core/src/testFixtures/java/xyz/zlatanov/frakkintoasters/fuzz/`, so the backend's tests can use
it for the regression journals. No class there ends in `EventProcessor` (the registry scans the root package).

- **`AnswerFinder`**: for a `PlayerDecisionEvent`, the concrete answer classes (the action class itself, or for an
  interface every production record from `EventCatalog.types()` that implements it and has a processor; never a
  `SandboxEvent`, an `AnytimeEvent`, `PlayerDecisionEvent` or a test fixture event), in random order. For each class it
  draws candidates from its source, puts the decision's constraints on them (`ConstrainedEvent.withConstraints`, as
  `submit` does), and asks `ProcessorRegistry.create(c).prepare(view, c)`. The first legal candidate of the first class
  that has one is the answer, so the pick is uniform over the classes that can answer. A class whose static gate says
  no is skipped (`SkillCardPlays.canPlay`, `QuorumCardPlays.canPlay`, `MutinyCardPlays.canPlay`,
  `CharacterActions.canUse`, `CharacterMovements.canUse`, `LocationEvent.canAct`). Before it reports NO_ANSWER it tries
  again exhaustively (up to 20,000 candidates a class).
- **Candidate sources**: hand-written ones where the answer has a shape, a reflective one for every other record.
    - Hand-written (the rules come from the processors' `isValid`):
        - `DrawSkillCardsEvent`: every multiset of the constraint's count over the 6 colors in random order, then 1
          fewer
          down to 0 (every site passes a count).
        - `DiscardSkillCardsEvent`: sub-multisets of the live hand of the size the constraint gives (exactly k, down to
          the hand limit, down to 1 or 3). `DiscardQuorumCardsEvent`: the President's hand less 10.
        - `PlayCardsBeforeSkillCheckEvent` and `PlayCardsIntoSkillCheckEvent`: none, then sub-multisets of the hand
          (into:
          sizes `PlayCardsIntoSkillCheckEventProcessor.canContribute` allows).
        - `TiebreakCardsBeforeSkillCheckEvent`: one card of each group of `SkillCheckView.playedBeforeConflicts()`.
        - `GlimpseTheFaceOfGodPlaceEvent` and `SetTheAgendaChoiceEvent`: ordered lists of 2 cards (fewer when the hand
          has fewer); `ReorderTopQuorumCardsEvent`: permutations of the top 5 Quorum Cards; `HumanDelusionEvent`:
          non-empty
          sub-multisets.
        - `PlayInterruptCardEvent`: the pass, each hand card of the window (`Calculations` with +1 and -1);
          `UseRollAbilityEvent`: the pass, `HOTSHOT` with a hand card, `LONGSHOT` with 1 to 8, the others bare.
        - `ChooseCrisisTargetEvent`: the last Crisis Card of the turn and `legalTargets`.
        - Any `ViperActivation` component: `ActivateViperEventProcessor.activations(view, fighter, false)` for every
          fighter in space, every launch (`VIPER`, `VIPER_MARK_VII`, `ASSAULT_RAPTOR` × `VIPER_LAUNCH_SPACES`), and
          null.
    - Reflective, per record component, by type and name (`ComponentRole`, see decision 4):
        - `playerNumber` of a `PlayerEvent`: the asked player. Other player ints: 1 to n.
        - Ship ints and lists: the ids of the ships on the boards (lists: subsets of up to 3).
        - `SkillCard`: the asked player's hand, plus the active check's revealed cards. `List<SkillCard>`: sub-multisets
          of
          the hand.
        - Card enums: the pile they come from (the player's Loyalty, Super Crisis, Motive and Mutiny Cards, the
          President's Quorum hand, the turn's Crisis Cards). `Character`: every value. Other enums: every value.
        - `List` of an enum: subsets of 1 and 2; `boolean`: both; count ints: -1 to 8; `EventConstraint`: null (the
          engine overwrites it).
        - Null for every reference component, because clients may send null and `isValid` has to reject it, not throw.
        - A type it has no domain for is a `FUZZER` finding naming the component, so a gap shows up in the report.
    - The product of the domains is tried in full when it has at most 512 tuples, otherwise as 64 random ones.
- **`Playout`**: the agent sorts everything it iterates (options by `toString()`, classes by name, ships by id), so a
  seed replays the same game, also in a new JVM. It wraps `submit`, `choose` and `restore` in `catch (Throwable)`: an
  `Error` such as an `AssertionError` gets past the engine's rollback (`GameEngine.advance` catches `RuntimeException`
  only) and leaves the engine half-applied, so the game stops there.
- **Restore twins**: at 3 random pauses, `GameEngine.restore(engine.journal(), seed)`; the next input goes to both, and
  `GameSnapshot`, the statuses of every player and `pendingInput()` have to be equal. Play goes on with the twin. A
  last restore at the end compares the finished games. Three a game, because a restore replays the whole journal.
- **`Endings.classify(view)`** reads the final state, not the event that ended the game (`WinChecks` queue their
  `CylonsWinEvent` after any event): a resource at 0, 6 damaged Galactica locations, a centurion at `HUMANS_LOSE`, no
  character left to select (`SelectCharacterEventProcessor.canBeSelected`), Kobol reached (8 distance) with the humans
  winning, or Kobol with the Cylons winning through Personal Goals. A finished game that none explains is BAD_ENDING.
- **Caps**: 50 rounds (`turn().number() / players`), and 5,000 inputs within one player turn (the setup is turn 0).

### Findings

| Kind                   | Fatal | Meaning                                                                                                     |
|------------------------|-------|-------------------------------------------------------------------------------------------------------------|
| `CRASH`                | yes   | An accepted input rolled back (`FrakCallTheAdmiralException`) or threw an `Error`                           |
| `INPUT_CRASH`          | no    | Checking an input threw something other than `InvalidActionException` (e.g. a null field)                   |
| `NO_ANSWER`            | yes   | The asked player can't answer: a decision without a legal answer, or a choice whose options all lead to one |
| `DEAD_OPTION`          | no    | An option of a choice leads to a decision without an answer, other options are live                         |
| `RESTORE`              | yes   | `restore` threw, or the twin differs                                                                        |
| `NO_END`               | yes   | The round cap, or the inputs cap within a turn                                                              |
| `BAD_ENDING`           | yes   | A finished game that no ending explains                                                                     |
| `REJECTED_BUT_CHANGED` | yes   | A rejected input changed the game (phase 4)                                                                 |
| `FUZZER`               | no    | The fuzzer itself: no domain for a component, or `prepare` accepted what `submit` rejected                  |

A fatal finding ends the game; the others are noted once per game and play goes on. A finding's ID is `F-` and 6 hex
digits of its signature: the kind, the root exception class, the first frame in `..event..` or `..engine..` (class and
method, no line), and the event's simple name (for NO_ANSWER and DEAD_OPTION the decision's class and constraints). So
it stays the same when lines move.

## Decisions

1. **The fuzzer finds its own answers; B1 is not built here.** The roadmap's B1 (`LegalAnswers` in `core`, on the
   wire) comes after A. The fuzzer needs answers now, so it builds candidates and lets the processors judge them
   through `prepare`, which only reads (`ops` is set by `run`). That uses the rules as they are and adds no second rule
   set. B3 later swaps the sources for B1's enumeration and uses the fuzzer to check that it is complete.
2. **Uniform random play, no strategy.** A random answer among the legal ones, a random class among the classes that
   can answer. Random play says nothing about balance; the win rates and lengths in the report are a smoke test for
   broken distributions only (roadmap A).
3. **Choices look ahead.** `playOrPassDecisions` asks every player whether to play "Political Prowess", "Change of
   Plans" or "Declare Emergency", holder or not. Picking a decision option that has no answer would end almost every
   game in NO_ANSWER. The agent checks each decision option first (`choose` changes nothing until it is called) and
   reports a dead one as DEAD_OPTION. The fix for that one (open question 1, answered): the engine keeps asking every
   player, so the table doesn't learn who holds the card, but a player without it is offered only the pass. That
   choice has a single option, so it is built as a `OneOf` directly: `one(...)` turns one option into the event itself,
   which would run without asking. The others see only who is asked (`GameProjector`'s `WaitingFor`), not the options,
   so a choice with only the pass looks like any other.
4. **`WireSchema.role` moves into `core`** as `event/ComponentRole` (player, ship, scope, count by component name),
   and `WireSchema` calls it. The fuzzer needs the same names, and so does B1. `WireSchemaTest.everyIntHasARole` keeps
   guarding it.
5. **`GameSnapshot` gets the 4 fields it misses**: `TurnView.startLocation()`, `sentToBrigAtEndOfTurn()`,
   `actionCostPaid(n)` and `SkillCheckView.cardsDivided()`, so a twin that differs there is caught.
6. **The run is a Gradle task, not a test.** `:core:fuzz` is a `JavaExec` of `FuzzRun` on the test fixtures'
   runtime classpath, with `enableAssertions = true` (a `JavaExec` runs without `-ea`, and without it a bad input
   corrupts a deck silently). It isn't part of `build`; the nightly schedule comes with the CI of milestone F. `build`
   runs only the fuzzer's unit tests, which can't go red because of an open finding.
7. **One report, generated and hand-written parts.** `fuzzer-report.md` in the repo root, checked in. The run rewrites
   only the block between `<!-- fuzz:begin -->` and `<!-- fuzz:end -->`, and keeps the history rows it finds there.
   The "Fixes" and "Questions" sections below it are written by hand during the fix loop. The run reads their IDs to
   mark a finding `question` or `regressed`. A single-seed run doesn't touch the report.
8. **A fixed engine bug leaves two tests.** A regression journal with the input that failed (the existing
   `RegressionJournals` pattern, which needs the backend's `JournalCodec`), and a test next to the fix in `core`
   (processor or engine test, as for any rule). The journal is the bug as found, the core test the rule as fixed. A
   seed isn't a regression test: any change to the engine sends the same seed down another path. A fuzz journal can't
   be exported again after its fix (the seed no longer fails), so when `FORMAT_VERSION` changes and a fuzz journal no
   longer loads, it is deleted and its core test stays (open question 2, answered).
9. **Threads.** Games share nothing but the scanned registries. `ClasspathScanner` opens a zip file system per jar,
   which two threads can't do at once, so `FuzzRun` touches `EventCatalog.types()` and `ProcessorRegistry` before it
   starts the pool. Results are sorted by player count and seed, so the report doesn't depend on the threads.
10. **The 32-file rule** (fix loop): the count is every path in `git status --porcelain --untracked-files=all` except
    `fuzzer-report.md`, so tests, journals, `choices.md` and `CLAUDE.md` count. It is counted after a fix is complete
    and green, never in the middle of one, so the tree is never left broken at a pause.

## Phase 1 - Playing one game

- `ComponentRole` in `core` (`event/`), `WireSchema.role` delegating to it.
- `GameSnapshot`'s 4 fields.
- `fuzz/`: `Playout`, `PlayoutResult`, `AnswerFinder`, the candidate sources, `Finding` with its kind and ID,
  `Endings`, and the restore twins.
- Tests (in `build`):
    - `AnswerFinderTest`: at every pause of `ScriptedTurns`, the finder finds an answer, and the answer it finds is
      accepted by `submit` on a restored copy.
    - The hand-written sources against hand-made states: a discard down to the hand limit, a draw with a color left
      empty, a tiebreak, a Calculations ±1.
    - `EndingsTest`: each ending from a hand-made final state, and a finished game that none explains.
    - `PlayoutTest`: 2 rounds from `ScriptedTurns.start()` (its stacked Crisis Cards), twice with the same seed: the
      same
      inputs both times, the twins match, and no `FUZZER` finding. Engine findings don't fail it, so an open finding
      never turns `build` red during the fix loop.

## Phase 2 - The run and the report

- `FuzzRun` (arguments `--games`, `--players` such as `3-7` or `4,6`, `--first-seed`, `--seed`, `--threads`,
  `--rounds`, `--report`, `--traces`), the thread pool, and the commit (`git rev-parse --short HEAD`, the number of
  changed files) for the report's header.
- `:core:fuzz` in `core/build.gradle`, mapping `-Pgames=`, `-Pplayers=`, `-PfirstSeed=`, `-Pseed=`, `-Pthreads=`,
  `-Prounds=` onto the arguments. Defaults: 100 games per player count, 3-7 players, seeds from 1, threads = cores - 1.
- `FuzzReport` (format under "The report") and the traces: the finding, the stack, the pending input, and the last 200
  inputs with turn, step and player. With `-Pseed=` the trace holds every input.
- Tests: `FuzzReportTest` writes a report from fixed results into a file with hand-written sections and checks that
  they stay, that the history keeps its rows, and that an ID under "Fixes" seen again is `regressed`.
- The first run, `-Pgames=20`: the report exists, and its findings are the fix loop's start. Nothing is fixed in this
  phase.

## Phase 3 - Regression journals with the failing input

- `RegressionJournals` reads an optional `input` (`player`, and an `event` or a Followup `option`, through
  `JournalCodec`) and `expect`: `runs` (applying the input throws nothing but `InvalidActionException`) or `answers`
  (the finder finds an answer at the pause, or for the option's decision, or the option is no longer offered).
  `RegressionJournalsTest` adds a dynamic test per file with an `input`. `CLAUDE.md`'s "Regression journals" bullet
  says that a fuzz journal a format change makes unloadable is deleted, and its core test stays (decision 8).
- `FuzzJournalExportTest` (backend tests, runs only with `-DfuzzJournal=4:117`, like `-DwriteExampleJournal`): plays
  the seed with `Playout`, takes the journal at the pause before the finding and the input that failed, and writes
  `backend/src/test/resources/journals/fuzz-4p-117.json`. `backend/build.gradle` passes `fuzzJournal` through.
- Tests: a journal made from `ScriptedTurns` without its last input, with that input as `input`, goes through write,
  read and apply.

## Phase 4 - Hostile inputs

Beyond the roadmap's four asserts, and the one that can be dropped: `isValid()` is the only check a client's input
gets (`CLAUDE.md` "Validation"), and the narrow domains above never send a card the player doesn't hold.

- At 1 in 20 decision pauses, `Playout` first submits a candidate from wide domains: any card of the decks, players 0
  to n + 1, unknown ship ids, nulls.
    - Rejected: the snapshot and the pending input have to be as before, otherwise REJECTED_BUT_CHANGED.
    - Accepted and fine: play goes on with it.
    - Threw anything else: INPUT_CRASH or CRASH.
- Tests: a probe against the `MoveEvent` discard of a card not in the hand is caught (it is a known bug, see
  "Expected first findings"; the test asserts the finding, and the fix loop flips it).

## Phase 5 - Docs, then a pause

- `CLAUDE.md`: `./gradlew :core:fuzz` under "Build Commands", a "Random playouts" paragraph under "Testing Patterns"
  (the package, the sources, the findings, the report, the regression journals with an input), and the "Roadmap" line
  for A (in progress until its exit). Each earlier phase already fixes the lines it made wrong (`WireSchema.role`).
- `roadmap.md`: A points to this plan and the report.
- **Stop for review and commit.** The fix loop counts files from a clean tree, so the fuzzer goes in first.

## The fix loop

Run, fix, record, and pause for a commit once more than 32 files changed:

1. `./gradlew :core:fuzz -Pgames=20` (3-7 players) refreshes the report. The batch grows when no open finding is left
   at its size: 20, 100, 1,000, then 10,000 per player count (the exit; it runs in the background).
2. Take the open finding hit by the most games (then the lowest player count and seed). Reproduce it with
   `./gradlew :core:fuzz -Pplayers=N -Pseed=S` in a fresh JVM. If it doesn't reproduce, the engine iterates a hash
   somewhere: that becomes the finding.
3. Triage:
    - **Engine bug**: export the journal (`./gradlew :backend:test --tests '*FuzzJournalExportTest' -DfuzzJournal=N:S`),
      see `RegressionJournalsTest` fail, fix it at the root the way `CLAUDE.md` says (a check that rejects instead of
      throwing, a decision offered only when it has an answer, facts through the ops), add the core test next to the
      fix, and correct any `CLAUDE.md` line it makes wrong.
    - **Fuzzer bug** (a missing domain, a NO_ANSWER the exhaustive search should have found): fix the fuzzer, with a
      test in `fuzz/`.
    - **A rules or design call** the rulebook and `choices.md` don't settle, or a fix that would by itself change more
      than 8 files: write it under "Questions" with the options and a recommendation, and go to the next finding.
4. `./gradlew :core:test` and `./gradlew :backend:test --tests '*RegressionJournalsTest'` green, the seed run no longer
   hits the finding, and an entry under "Fixes" (a ruling also goes into `choices.md`).
5. Count: `git status --porcelain --untracked-files=all | grep -v ' fuzzer-report.md$' | wc -l`.
    - 32 or fewer: back to 2 (to 1 after every few fixes, so the report shows what changed).
    - More than 32: **pause.** Start Docker Desktop and run `./gradlew build`, rerun the batch so the report is current,
      and stop with: the fixes of this batch (IDs, one line each), the files, the tests, the open findings and the
      questions, and `needs input:` to review and commit. Go on only when the user says so, on a clean tree.

## The report

`fuzzer-report.md`, example of the generated block and the hand-written sections:

```markdown
# Fuzzer report

Random games played by `./gradlew :core:fuzz` (see `fuzzer-plan.md`). The block between the markers is rewritten by
every run; "Fixes" and "Questions" are written by hand.

<!-- fuzz:begin -->

## Last run

2026-11-24 10:12, `108f934` (3 changed files), seeds 1-100, 3-7 players, 6m 10s.

| Players | Games | Finished | Failed | Humans won | Cylons won | Rounds (median / max) |
|---|---|---|---|---|---|---|
| 3 | 100 | 97 | 3 | 14 | 83 | 9 / 31 |
| All | 500 | 488 | 12 | 61 | 427 | 9 / 38 |

### Endings

| Ending | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|
| Food at 0 | 31 | ... |
| Kobol, humans win | 14 | ... |

### Findings

| ID | Kind | What | Games | Example | Status |
|---|---|---|---|---|---|
| F-1c2d3e | INPUT_CRASH | `NullPointerException` in `DeclareEmergencyEventProcessor.isValid` | 212 | 3p seed 4 | open |
| F-9a0b11 | DEAD_OPTION | `PoliticalProwessEvent` for a player without the card | 498 | 3p seed 1 | question |

- **F-1c2d3e**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=4`, trace `core/build/fuzz/F-1c2d3e-3p-4.txt`. Turn 3,
  `SKILL_CHECK`, player 2 asked for `DeclareEmergencyEvent`.

### Watch

- 4 players: no Kobol ending in 100 games.

<details><summary>Event types that never ran (41)</summary> ... </details>

### History

| Run | Commit | Games | Failed | Open findings |
|---|---|---|---|---|
| 2026-11-24 10:12 | `108f934` | 500 | 12 | 5 |

<!-- fuzz:end -->

## Fixes

### F-1c2d3e - INPUT_CRASH - fixed 2026-11-24

- Cause: `isValid` read `card.type()` of a null card.
- Fix: the three play-or-pass processors reject a null card.
- Tests: `DeclareEmergencyEventProcessorTest.rejectsNoCard`, journal `fuzz-3p-4.json`.
- Files: `DeclareEmergencyEventProcessor.java`, ...

## Questions

- **F-9a0b11** (DEAD_OPTION): every player is asked whether to play "Political Prowess", holder or not ... Options: ...
  Recommendation: ...
```

- Statuses: `open`; `question` when the ID is under "Questions"; `regressed` when it is under "Fixes" and seen again.
- "Watch": a team that never wins, or an ending that never happens, over the run (a smoke test, not balance).
- "Event types that never ran": the event classes of `EventCatalog` no game opened a scope for, so the report shows
  what random play doesn't reach.

## Expected first findings

Found while reading the code for this plan. They are not fixed up front: the fuzzer has to find them, and they go
through the fix loop like any other.

- `GameEngine.advance` catches `RuntimeException` only, so a failed `assert` leaves a scope open and the game
  half-applied (CRASH).
- `MoveEventProcessor.isValid` doesn't check that `discardCard` is in the hand (phase 4), and a pilot's move to a Cylon
  Fleet space unboxes a null distance (`DISTANCE_LOOKUP_TABLE` has only Galactica's space) (INPUT_CRASH).
- `isValid` throws on a null card in `ChangeOfPlans`, `DeclareEmergency`, `PoliticalProwess`, `DiscardQuorumCards` and
  `TiebreakCardsBeforeSkillCheck` (INPUT_CRASH).
- `playOrPassDecisions` offers the play to every player, holder or not (DEAD_OPTION; the fix is in decision 3).
- `SkillCheck.playedBeforeConflicts` groups with a `HashMap` keyed by an enum, so the tiebreak options come in an
  order that can change between JVM runs (the journal is unaffected, the order a player sees isn't stable).

## Open questions

1. **Play or pass for everyone** (decision 3). Answered 2026-11-22: ask everyone, and offer a player who doesn't hold
   the card only the pass.
2. **Regression journals and `FORMAT_VERSION`** (decision 8). Answered 2026-11-22: when a format change makes a fuzz
   journal unloadable, delete the journal and keep the core test that covers the bug.
3. **Caps**: 50 rounds and 5,000 inputs a turn are guesses; the first runs' "Rounds" column says whether they fit.
   The first run (100 games): 44 rounds at most, and no NO_END.

## Not in this plan

- B: legal answers in `core` and on the wire. The fuzzer's sources are its stand-in.
- Scheduling the nightly run (milestone F's CI).
- A playing strategy or bots (G), balance work beyond the readout.
- `undo` checks and the backend's sessions under random play.
- Engine performance work, unless a 10,000-game run takes longer than a night.

## Critical files and reuse

- New: `core/src/testFixtures/java/xyz/zlatanov/frakkintoasters/fuzz/` (`Playout`, `PlayoutResult`, `AnswerFinder`,
  the sources, `Finding`, `Endings`, `FuzzRun`, `FuzzReport`), `core/src/main/.../event/ComponentRole.java`,
  `backend/src/test/.../sandbox/FuzzJournalExportTest.java`, `fuzzer-report.md`.
- Changed: `core/build.gradle` (`fuzz`), `core/src/testFixtures/.../engine/journal/GameSnapshot.java`,
  `backend/src/main/.../wire/WireSchema.java`, `backend/src/test/.../sandbox/RegressionJournals.java` and
  `RegressionJournalsTest.java`, `backend/build.gradle` (`fuzzJournal`), `CLAUDE.md`, `roadmap.md`.
- Reused: `GameEngine.start/restore/submit/choose/pendingInput`, `ProcessorRegistry.create` and `hasProcessor`,
  `EventProcessor.prepare`, `ConstrainedEvent.withConstraints`, `EventCatalog.types()`, the static gates
  (`SkillCardPlays`, `QuorumCardPlays`, `MutinyCardPlays`, `CharacterActions`, `CharacterMovements`,
  `LocationEvent.canAct`), `ActivateViperEventProcessor.activations`, `GalacticaBoardView.VIPER_LAUNCH_SPACES`,
  `ChooseCrisisTargetEventProcessor.legalTargets`, `PlayCardsIntoSkillCheckEventProcessor.canContribute`,
  `SkillCheckView.playedBeforeConflicts()`, `SelectCharacterEventProcessor.canBeSelected`, `GameSnapshot.of`, the
  `TurnLoopTest.assertRestoresTheSameGame` pattern, `TestEngines`, `ScriptedTurns`, `JournalCodec` and the
  `RegressionJournalsTest.writeExample` pattern.

## Verification

- Each phase: `./gradlew :core:test`; phase 1 also `./gradlew :backend:test --tests '*WireSchemaTest'`; phase 3
  `./gradlew :backend:test --tests '*RegressionJournalsTest'`.
- Phase 2: `./gradlew :core:fuzz -Pgames=20` writes `fuzzer-report.md` with every section, `-Pplayers=4 -Pseed=1`
  writes a full trace and leaves the report alone, and two runs of the same seed give the same trace.
- Phase 3: an exported journal of a real finding fails `RegressionJournalsTest` before its fix and passes after.
- Phase 5 and every pause: `./gradlew build` with Docker Desktop running.
