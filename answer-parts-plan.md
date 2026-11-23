# Answer parts plan: pick the parts of an answer, not one of every combination

## Context

`AnswerSearch` (core) tries every tuple of a record's scalar components, so a record with two or more varying scalars
comes out as one concrete answer per combination. The panel then shows one chip per answer. `EngineRoomEvent(int
playerNumber, SkillCard discardCard1, SkillCard discardCard2)` with a hand of 8 is 56 chips ("Repair 1 · Bait 0",
"Bait 0 · Repair 1", ...). Lists already work: a list component is a shape, and the `PickWidget` lets the player pick
cards out of a pool. Draws work too: the `CountsWidget` builds a value and checks it against the answers.

The fix is the `CountsWidget` idea used more widely. The answers stay what they are and act as the check. The panel
shows the pool of values for each part of the answer, the player picks a value per part, and a value stays enabled only
while some answer still fits. Once one answer is left, the panel sends it. The engine still judges what is sent. Core,
the wire and the journal format don't change.

### What lists combinations today (the analysis)

Groups whose moves differ in more than one component, so the chips are a product:

- **Two cards from the hand**: `EngineRoomEvent` (the reported case).
- **Target × something**: `MakeADealEvent` (target × destination × drawer, the largest product: hundreds of answers),
  `AttorneyEvent` and `PresidentialPardonEvent` (target × destination), `PresidentialOrderEvent` (× title),
  `IntimateEvent` (× deck), `PoliticallyAdroitEvent` (× card), `CultLeaderEvent`, `PassMutineerCardEvent`,
  `UseProbationEvent`.
- **Card × target/where**: `ExecutiveOrderEvent`, `FullThrottleEvent`, `UnorthodoxPlanEvent`, `PreventivePolicyEvent`,
  `RaptorSpecialistEvent`, `LaunchReservesEvent` (card × area × fighter list), `CombatVeteranEvent`, `DutifulEvent`.
- **Ships**: `PassCagTitleEvent` (activation × target), `CivilianSelfDefenseEvent`, `EscortEvent`, `DogfightEvent`,
  `BasestarBridgeShipsEvent`, `LaunchViperEvent`, `PrimacyEvent`.
- **Interrupt windows**: `PlayInterruptCardEvent` (card × Calculations' ±1), `UseRollAbilityEvent` (ability × card ×
  result).
- **A choice (OneOf) built as a product**: `ChooseGalacticaDamageTokensEvent`, every pair of up to 5 tokens (10
  options), shown today as 10 option buttons.

Already fine, and left alone: list shapes (`DiscardSkillCardsEvent`, `PlayCardsIntoSkillCheckEvent`, Repair's vipers,
...), the move's discard pick, the draw counters, and groups where only one component varies (a target, a section, a
card: one chip per value, which is linear).

## Target design

```text
group of moves (one kind)           frontend/src/input/parts.ts (new, pure)
   partsOf(group): Part[]           the varying components, minus any shape's pick component;
   |                                numbered siblings of one type (discardCard1, discardCard2) merged into one part
   |                                of several elements, in any order
   distinct(moves, parts)           one move per tuple of part values (Engine Room's A·B and B·A are one answer),
   |                                shape-or-not kept apart
   isProduct(distinct, parts)       more distinct moves than the largest part has values -> the parts widget,
   |                                otherwise chips as today (3 cards of Engine Room = 3 chips)
   v
PartsWidget (AnswerPanel.tsx)       a row per part: its values as tiles (cards, ships with art) or chips
                                    (players, locations, enums, numbers), "choose n" for a part of several
   fitting(moves, parts, picks)     moves whose scalar parts equal the picks and whose multi parts contain them
   allowed(..., part, value)        a value is enabled while fitting(picks + value) is not empty
   settled(...)                     each part is either picked equal to the value every fitting move shares, or
                                    untouched with every fitting move agreeing on it (shown as implied, e.g. the
                                    damage tokens returned once the 2 resolved are picked)
   all settled, 1 move   -> "<group title>" button sends it (a shape opens its PickWidget, as a chip does)
   all settled, 2+ moves -> those moves as chips under the rows (e.g. Repair: the location or the vipers)
```

`Part = { key, components: string[], type: TypeRef, many: boolean, values: unknown[] }`. A scalar part's values are
the distinct values in order of first appearance, null included (labelled "none"). A part of several elements (a list
component with concrete values, or merged siblings) uses a pool: each element as many times as the most any one move
holds it, by position like `PickWidget`. Skill Cards are sorted with `sortedPositions`. Matching uses `canonical` and a
multiset `within` from `answers.ts`, so lists compare as multisets, the same way `covers` and the engine do.

## Decisions

1. **Frontend only.** The answers are the check, as the request says. Changing `EngineRoomEvent` to a list would give
   it a core shape, but it would also bump `FORMAT_VERSION` for one record, and the other products (targets ×
   destinations, choices) would still need this anyway.
2. **The product rule decides, not a list of events.** A group gets the widget when its distinct moves outnumber its
   largest part. That covers every record above, and any record added later, without naming them. One varying
   component never qualifies, so today's chips stay as they are.
3. **Numbered siblings are one part.** Components of the same type whose names differ only by a trailing number are
   picked together, without order. Today that's only Engine Room. `ReligiousVisionsEvent`'s two Crisis Cards have
   different names, so they stay two parts.
4. **Board picks still narrow first.** Clicks on the board and the mats narrow the moves (`narrow`) before the panel
   groups them, so the widget only shows what is left. The widget's own picks stay local and aren't pushed to the board:
   `narrow` matches a target in any component, and a part is one component.
5. **Choices get it too.** In `ChoiceAnswers`, plain options (not decision sections) of one event kind that form a
   product are shown as one `PartsWidget` where the first of them would be. Sending it chooses that option's index.

## Phases (each ends green)

### 1. The model: `parts.ts`
- `frontend/src/input/parts.ts`: `Part`, `partsOf`, `distinct`, `isProduct`, `fitting`, `allowed`, `settled` (returns
  the fitting moves once every part is settled, otherwise null). Export `within` from `answers.ts` and reuse
  `canonical`, `varyingComponents` and `isServerFilled`.
- `frontend/src/input/parts.test.ts` covers: Engine Room siblings merged, ordered duplicates collapsed, two copies of a
  card (a pair of the same card only when the answers hold it), 3 cards → not a product, 4 → product; a three-way
  scalar product (MakeADeal-like); damage tokens (two list parts, the returned one implied); `UseRollAbilityEvent` with
  nulls (Longshot leaves the card as implied "none"); a group mixing a concrete move and a shape (Repair) that ends in
  2 settled moves.

### 2. The widget in the panel
- `AnswerPanel.tsx`: in `GroupView`, `isProduct` → `PartsWidget` instead of the chips (the `CountsWidget` branch stays
  first). Chips render `distinct` moves, so the Engine Room's 3-card case shows no A·B/B·A duplicates. `ChoiceAnswers`
  groups the plain options by kind (`groupMoves`) and renders a product group as a `PartsWidget`. Widen `elementArt` to
  `valueArt(value, type, table)`: a SkillCard → `skillCardArt`, an enum → `enumArt` (`art/images.ts`), a ship id →
  `shipArt`; a label from `valueLabel` (`labels/describe.ts`).
- `moveLabels.ts`: `partName(part)` (the humanized component name without the sibling digit or the `Number`/`Id`
  suffix: "Discard card", "Target player", "Civilian ship") and `partHint(part, sizes)` ("discard 2 cards", like
  `pickHint`).
- `styles.css` (the "table" section): `.parts-widget` rows reuse `.picker`, `.tile.card-pick` and `.chip`. Add an
  `implied` state for settled parts.
- `AnswerPanel.test.tsx`: Engine Room with 4 cards shows one pool of 4 tiles; Send stays disabled until 2 are picked;
  after 2 the rest are disabled; it sends the matching answer as it was listed. A damage-token choice of 6 options
  sends the option index. Engine Room with 3 cards still shows 3 chips. The popover uses `Groups`, so it gets the same
  widget.

### 3. e2e and docs
- `frontend/e2e/playthrough.spec.ts`: count `.parts-widget` among the candidates. To fill one, pick an enabled,
  unpressed value at random in a part that isn't settled, until the send button is enabled (then click it) or the
  fallback chips show (click one). On a dead end, use "Clear".
- `CLAUDE.md`, Frontend → "Answers": one sentence on the parts widget (the product rule, siblings, the check against
  the answers).

## Verification

- `npm test` in `frontend/` (Vitest: `parts.test.ts`, `AnswerPanel.test.tsx`, `answers.test.ts`), then `./gradlew
  build`. It type checks and builds the frontend, and touches no Java. Even so, don't run it while a bootRun JVM is up.
- Manual check in a sandbox on the user's running servers (5173 hot-reloads the working tree): put a character on the
  "Engine Room" and give him 5 Skill Cards, then take an action. Expect one pool of 5 cards, "discard 2 cards", Send
  after two picks, and the log showing the discard. Also give a revealed Cylon "Damage Galactica" and expect a pool of
  5 tokens with the returned ones implied. Also try "Make a Deal" with three rows.
- `E2E_BASE_URL=http://localhost:5173 npm run e2e -- playthrough.spec.ts` (trace off). It should reach an ending with
  "Advanced" never opened.

## Not in this plan

- Changes to `AnswerSearch` or the records (e.g. making `EngineRoomEvent` a list). The answers payload keeps its
  combinations: at most a few hundred events, already judged today.
- Pushing the widget's picks to the board's highlights.
