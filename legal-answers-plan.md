# Legal answers plan (roadmap milestone B)

Status (2026-11-22): done, all four phases. Where phase 3 differs from the plan:

- `ComponentRole` names `min`, `max` and `minStrength` counts, so the picks pass `WireSchemaTest`; `WireCodecTest`
  checks that the answers carry their `kind`, `LiveGameTest` that a secret input's answers reach only its holder.
- The move prompt lists the pick's cards and has no "No discard" any more: a move that needs no discard is sent at once.
- The sandbox e2e spec was out of date before this milestone (checked on `78a0421`): seed 42 seats player 3 first, and
  Starbuck's "Secret Destiny" asks before the stacked Crisis Card resolves. It now takes whoever is seated first and
  lets the card resolve.

Where phase 2 differs from the plan:

- The fuzzer keeps `Domains.wide` and the hostile candidates; `fuzz/AnswerClasses` became `ProductionEvents` (the
  report's list of events that never ran), `Combinations` went with the candidate sources. A mutation check runs at 1
  in 10 answers (`Playout.Options.mutationChance`).
- 50,000 games (10,000 seeds per player count) take 15 minutes, as long as with the fuzzer's own search. The runs found
  a draw's zero counts (`covers` compares counts without them), the cap of 20 elements on a pick ("Command
  Authority" leaves a hand of 30), and two stale queued events of the engine: the attack result after "Hotshot" sent
  the pilot to the "Brig", and "Assume Command"'s title after its "Bait" did (`fuzzer-report.md`'s "Fixes", two
  regression journals). The last run has no finding but the NO_END games: 278 with every human in the "Brig" (the
  valid stall) and 51 slow games that end on their own in 51 to 82 rounds.

Where phase 1 differs from the plan:

- A ship id's values are narrowed by its name (`civilianShipId`, `raiderId`, `basestarId`, `cylonShipId`,
  `viperId`/`fighterId`, `centurionId`), and the list groups too: trying every ship for each id made "Communications"
  thousands of candidates. A draw tries up to the count of its constraint. The search judges two witnesses a size, the
  first and the last elements, and the sources order their elements for it.
- `MoveEventProcessor` now asks `Actions.canMove`: it took a move out of the "Brig" that nobody is asked for.
- A cross-check against the fuzzer's own search over 60 games (16,379 decisions) found every answer it found and no
  answer the game refuses; an `ActionEvent` decision takes 10 ms on average, a draw of 5 cards 2 ms.

Notes from reading the code for phase 1:

- The decisions come from `new PlayerDecisionEvent<>(n, X.class)` across the processors, plus four helpers:
  `FollowupUtils.eachPlayerFromTheLeft` (`PlayCardsBeforeSkillCheckEvent`, `PlayCardsIntoSkillCheckEvent`),
  `OfferPlayOrPassEventProcessor` (the "Political Prowess", "Change of Plans", "Declare Emergency" events),
  `OpenInterruptWindowEventProcessor` (`PlayInterruptCardEvent`, `UseRollAbilityEvent`), and the Quorum/Mutiny
  `eventClass(card)` decisions (`SkilledPoliticianChoice`, `AbusePowerChoice`, `PopularInfluenceChoice`,
  `DutifulAgain`). `MakePresidentEvent(Character)` is a plain `Event` answer. No decision asks for `Event` or
  `PlayerEvent`, so no test-fixture event can answer one and core needs no "main code only" filter.
- Answers with a list component that need a source in `Shapes`: `DrawSkillCards` (concrete maps),
  `DiscardSkillCards`, `DiscardQuorumCards`, `PlayCardsBefore` (distinct `playableBeforeSkillCheck` cards),
  `PlayCardsInto`, `HumanDelusion`, `TiebreakCardsBefore` (concrete, `Combinations.oneOfEach`), `GlimpseTheFaceOfGodPlace`
  and `SetTheAgendaChoice` (ordered 2), `ReorderTopQuorumCards` (ordered, `InfluentialEventProcessor.top`), and the
  `ActionEvent`s `Sacrifice` (per ship kind in her area), `StarPlayer`, `Treatment` (concrete color pairs),
  `AssumeCommand`, `Impeachment`, `AuthorizationOfBrutalForce` (singletons concrete, raiders a pick), `Resignation`
  (hand less the card), `ResourcesForGalactica`, `LaunchReserves` (concrete type lists), `Repair`,
  `RevealPersonalGoal` ("Selfish": strongest first, `minStrength` 20). `MoveEvent.discardCard` is a one-card pick.
  The other list-bearing events (`ChooseDestination`, `GasCloudPlace`, `TreatmentGive`, ...) are choice options.
- The fuzzer's `Domains.of` adds the skill check's pile (`cards().cards()`) to the `SkillCard` domain; core must add it
  only once `cardsDivided()` (decision 6).
- An int-keyed map would fail `WireSchemaTest.everyIntHasARole`, hence `OptionAnswers(option, answers)` (`option` is
  already a COUNT name in `ComponentRole`).

Implements milestone B of `roadmap.md`, "Legal-move hints": a list in `core` of the legal answers to a pending
decision, carried to the asked viewer in the projection's `input`, and the fuzzer answering from it.

Today the engine publishes no legal answers. The table guesses: `boardMoves.ts` offers every location off space as a
move and asks for a discard when a move "leaves its area", the "Act" button shows whenever an action option exists,
and the event builder lists every event type of an action (dozens for `ActionEvent`) with a field per component. The
fuzzer has its own candidate search (`fuzz/Candidates`, `Domains`, `AnswerClasses`), which only it can use.

The plan is done when:

- `LegalAnswers.of(game, decision)` gives, for every decision the game can ask, the concrete answers and the shaped
  ones (a list of cards or ships to pick), each judged by the processors' own `isValid()`;
- the asked viewer's `input` carries them, for a decision and for each option of a choice that asks one;
- the fuzzer answers from them and checks that they are complete: 10,000 seeds per player count with no decision
  whose answers are empty and no legal input outside them.

## Target design

```text
LegalAnswers.of(game, decision)                                  core/.../engine/answer/
   |
   v
AnswerClasses.of(decision.action())   every production record that can answer: the action itself, or each record of
   |                                  an interface (ActionEvent: locations, cards, abilities, titles, ...), with the
   |                                  cheap check the game asks before it offers it (canPlay, canAct, canUse)
   v
for each class whose check is open:
   the scalar components   -> Domains: the values each can take now, by type and name (the asked player, his
   |                          cards, the ships on the boards, every constant of an enum, null)
   the list component      -> Shapes: where its elements come from (the hand, the President's hand, the
   |                          damaged vipers, ...), or the few list values to try as they are
   v
   every tuple of the scalar values, with the decision's constraints, judged by
   ProcessorRegistry.create(event).prepare(game, event)
      no list:    each legal tuple is a concrete answer
      a list:     one witness per size 0..n judged; size 0 is a concrete answer, each run of legal sizes a
                  ShapedAnswer(example, pick: SkillCardPick | QuorumCardPick | ShipPick (from, min, max, ordered))
   |
   v
LegalAnswers(events, shapes)        covers(event): is it one of them, or does it fit a shape
```

- **Where**: `core/src/main/java/xyz/zlatanov/frakkintoasters/engine/answer/`. The engine may use the processors and
  the events; the events may not use the engine (`ArchitectureTest`), and the projection reads the engine already
  (`PendingInput`).
- **`LegalAnswers(List<Event> events, List<ShapedAnswer> shapes)`**: every event is an answer `submit` takes, with the
  decision's constraints on it. `ShapedAnswer(Event example, Pick pick)`: the example is legal as it is, and the pick
  names the component to fill from `from`, between `min` and `max` elements, in order or not. A `SkillCardPick` also
  has a `minStrength` ("Selfish": 20). A pick on a single card (a move's discard) has `min = max = 1`.
- **`LegalAnswers.forOption(game, option)`**: the answers of the decision an option of a choice asks first (a decision,
  or the first event of an `all(...)`), empty for any other option. Choosing an option changes nothing before its
  decision is asked, so they are exact.
- **Projection**: `Decide(player, action, constraints, answers)` and `Choose(player, options, answers)`, the latter a
  list of `OptionAnswers(option, answers)`. Only for the viewer who answers (or a sandbox), so a secret input stays
  secret.
- **Fuzzer**: `AnswerFinder` picks a random class among the answers, then a random concrete answer or a random filling
  of a shape (judged again, the example when none fits). Its probes keep the wide domains (`Domains.wide`), and an
  accepted probe outside the answers is a `HINT_MISSING` finding, as is a mutation of a legal answer (one component set
  to each of its wide values) that the game accepts and the answers don't cover.

## Decisions

1. **Generate and judge, no second rule set.** The candidates come from the state, and `prepare` (the processor's
   `isValid()`) judges each, as it judges a client's input. Outside the processors the only knowledge is where a
   component's values come from (`Domains`, `Shapes`) and the cheap check that skips a class (`canPlay`, `canAct`,
   `canUse`, already the checks the game asks before it offers a card, an ability or a location).
2. **Concrete where the set is small, shaped where it is a pick.** A record whose components are scalars is enumerated
   in full. A list component of cards or ships is a pick: its elements and the sizes that are legal. Lists of few
   values are tried as they are instead, as concrete answers: the colors of a draw (at most 462 selections for 5
   cards), "Treatment"'s 2 colors, "Launch Reserves"' fighter types, the tiebreak of the cards played before a check.
   A move's discard is a pick of one card, so a move is one answer per destination, not one per destination and card.
3. **Sizes by witness.** For a pick the search judges, per size from 0 to the number of elements, a few subsets of that
   size (the first ones, the last ones); a size is legal when one of them is. Size 0 is a concrete answer, each run of
   legal sizes a shape. That is exact for rules that count ("discard exactly 3", "no more than 3 into the check", "none
   or 2 and more"). Where a rule reads the cards, a shape can hold fillings the engine rejects ("Selfish"'s strength,
   one Reckless card before a check): the example is legal, the engine judges the rest, and a source orders its
   elements so the witnesses find the sizes (the strongest cards first for "Selfish").
4. **Sound and complete.** Every concrete answer and every example passes `prepare` (the tests check it per decision
   type). Every legal answer is a concrete answer or fits a shape (`covers`): the fuzzer checks it with probes and
   mutations. A list compares as a multiset in `covers`.
5. **A check that throws leaves its candidate out**, it doesn't break the table: a client could send that candidate
   too. A listener gets the exception, and the fuzzer reports it as an `INPUT_CRASH`. A class with a list component and
   no source in `Shapes` is skipped and reported the same way, and a test lists every answer class with a list.
6. **The answers say no more than a rejection would.** They are built for the asked player from his own cards and the
   public state, and only he gets them. A card of a skill check is a candidate once the cards are divided (face up).
7. **Choices too.** The Action step is a choice of `decision<ActionEvent>` or nothing, so its answers come with the
   option: the hand tray of milestone C can light the playable cards before the option is picked.
8. **Wire, not journal.** `LegalAnswers`, `ShapedAnswer`, `Pick` and `OptionAnswers` are projection types:
   `TypeRegistry.wire()` registers the sealed `Pick`, `WireSchema` exports them, `FORMAT_VERSION` stays as it is.
9. **B1, B3, B2.** The fuzzer checks the enumeration before the wire and the table depend on it.
10. **The fuzzer's candidate search moves to `core`.** `fuzz/AnswerClasses`, `Domains.of` and `Candidates` (but the
    hostile candidates) become `engine/answer/`. The fuzzer keeps the wide domains, which now also check completeness.
11. **The table stops guessing where the answers say it.** `boardMoves.ts` takes its move targets and the discard from
    the answers, and the "Act" button shows only when the location's action is among them. Everything else in the
    table is milestone C.

## Phases

Each phase ends with `./gradlew build` green (Docker Desktop running for the backend's Testcontainers) and the lines of
`CLAUDE.md` it makes stale fixed.

### Phase 1: `LegalAnswers` in core (B1)

- `engine/answer/`: `LegalAnswers`, `ShapedAnswer`, `Pick` (`SkillCardPick`, `QuorumCardPick`, `ShipPick`),
  `AnswerClasses`, `Domains`, `Shapes`, and the search.
- `LegalAnswersTest`, a test per kind of decision: every answer passes `prepare`, a known legal answer is there and a
  known illegal one is not. Moves (with and without a discard, the "Brig"), the Action step (a location, a Skill Card,
  a Quorum Card, a Mutiny Card, a character action, a Title Card), viper activations and launches, draws (a skill set,
  any color, an empty deck), discards (exactly n, down to the hand limit, the Quorum hand), cards before and into a
  skill check (the "Brig", "Impulsive"), the interrupt windows and roll abilities, crisis targets, the character
  choice, the orders ("Glimpse the Face of God", "Set the Agenda", "Influential"), "Selfish", "Sacrifice",
  "Authorization of Brutal Force". And: every answer class with a list component has a source.
- `CLAUDE.md`: a "Legal answers" section, the package list, the roadmap line.

### Phase 2: the fuzzer answers from them (B3)

- `AnswerFinder` answers from `LegalAnswers`; an empty one is `NO_ANSWER` (a decision) or `DEAD_OPTION` (an option).
- New finding `HINT_MISSING` (not fatal): an accepted probe, or an accepted mutation of an answer, that the answers
  don't cover. A mutation check at 1 in 10 decisions (an option).
- `fuzz/` loses what moved to core; `CandidatesTest` keeps the hostile candidates and the combinations, and its
  domain tests move to `LegalAnswersTest`.
- Run `./gradlew :core:fuzz` at 20, 1,000, then 10,000 games per player count, and fix what it finds (the fix loop:
  a core test next to each fix, an engine bug as a regression journal).

### Phase 3: the wire and the table (B2)

- `GameProjection.Decide.answers`, `Choose.answers` (`OptionAnswers`), built by `GameProjector` for the viewer who
  answers.
- Backend: `TypeRegistry.wire()` registers `Pick`; `./gradlew :backend:exportSchema`, `npm run gen`; a test that the
  asked player's `game` message carries the answers and the others' don't.
- Frontend: `boardMoves.ts` from the answers (targets, discard, "Act"), its tests, the e2e specs.

### Phase 4: docs

- `CLAUDE.md` (Projections, Frontend, Random playouts), `roadmap.md` B marked done, this plan's status.

## Open questions

None that block: the open questions of the roadmap don't touch this milestone.

## Not in this plan

- Any new input on the table (the hand tray, targets lighting up, pickers): milestone C uses these answers.
- Labels or explanations of an answer ("what it does"): C1 and C3.
- Caching the answers between pushes: they are built once per input for one viewer.
