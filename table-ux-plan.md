# Table UX plan (roadmap milestone C)

Status (2026-11-22): done, all seven phases. `playthrough.spec.ts` plays seed 7 to a Cylon win in 254 pauses (1.2
minutes) and seed 11 in 412 pauses, through the answers alone, with no filling refused and "Advanced" never opened.
Where the work differs from the plan:

- The Skill Card colors and the Attack Table come from the wire schema: `SkillCardType`'s `colors`, and `attackTable`
  from `AttackTable` in `core`, which reads the attack results' own thresholds (`AttackTableTest`). The frontend copies
  no game data.
- A sandbox shows the asked player's own cards face up on his mat, so the sandbox can play from the mat as he would.
- The intent stays pending (`sent`) until the game after its answer comes, and the panel is `aria-busy` meanwhile:
  without that, the panel showed the decision for a moment after its answer had left.
- The rulebook PDFs ship as Vite assets, not under `/rules/`. The section names and the cheat sheets are in our words,
  with the article and page to read.
- The log hides the scopes that only mark a turn or a step, and a story fact that repeats its line; a ship's move is
  worded by where it went (destroyed, damaged, launched, landed).
- The playthrough records no Playwright trace: a whole game is thousands of actions, and writing their snapshots took
  ten times longer than the game.

Implements milestone C of `roadmap.md`, "A table that is pleasant to play": C1 (inputs on the board and the hand), C2
(turn flow and awareness) and C3 (reference without leaving the table).

Today every decision is the `EventBuilder`: a "Which" dropdown of event types, then a field per record component. A
choice is a column of text buttons. `boardMoves.ts` covers moving and the "Act" button only. Since milestone B the
asked viewer's `input` carries the legal answers of every decision (`Decide.answers`) and of every option of a choice
that asks one first (`Choose.answers`), and the fuzzer has shown they are complete: every decision has answers, and
nothing outside them is taken.

The plan is done when:

- every pending input can be answered from the answers alone: in the panel as buttons, cards and pickers, on the
  board by clicking what lights up, on the mats by clicking a playable card; the builder only behind "Advanced";
- the table says what the game waits for, what changed and what happened, in words and on the board;
- the cards, the character sheets, the rulebook and a few cheat sheets are a click away;
- a seeded four-player sandbox plays from setup to an ending in the e2e without opening "Advanced".

## Target design

```text
input (Decide | Choose, with answers)                                frontend/src/input/answers.ts
   |
   v
moves(input): Move { option, event, pick }      one per concrete answer, shape, or option of a choice
   |
   +-- narrow(moves, picked)                    what is still possible after the clicks so far (the "selection")
   |
   +-- the panel        (AnswerPanel)           groups by event kind, a button per answer, a picker per shape
   +-- the board        (BoardSvg targets)      locations, ships and standees named by what is left, light up
   +-- the mats         (PlayersPanel)          the asked player's cards that start an answer, light up
   |
   v
send(move): choose(option) | submit(event) | choose(option) then submit(event) on the next pause (intent)
```

- `Move` is one way to answer the pending input: a concrete answer of a decision, a shape (an example with a pick to
  fill), an option of a choice, or an option followed by an answer of the decision it asks first (the Action step's
  "take an action" with the action itself). Sending an option with its answer chooses the option and, when the next
  pause is that decision for the same player, submits the answer (`gameStore.intent`); anything else drops it.
- A selection is a list of picked values (a card, a player, a ship, a location, an event kind). It narrows the moves
  to those that name all of them. One move left without a pick: it is sent. Several left: the panel, a popover next to
  what was clicked, and the board show only those. "Back" takes the last pick away and "Clear" all of them: the "take
  back" of C1, local to the browser until a move is sent.
- The board lights a target when the moves left name it: a `Location` constant or the `location` of a location
  action, a ship id (`role: ship`), another player (`role: player`, not `playerNumber`). The mats light the asked
  player's cards that a move names (a Skill Card, a Quorum, Mutiny, Loyalty, Motive or Super Crisis Card). A hover on
  a target shows what clicking it does when one move is left.
- The input names what it is about: `topic`, the event of the scope that queued the pending input, as public as the
  log shows it, on `Decide`, `Choose` and `WaitingFor`. The table turns it into a sentence: "William Adama decides:
  Crisis: Riots", "Waiting for Kara Thrace: Political Prowess offer".

## Decisions

1. **The answers are the input.** Every widget is built from `moves(input)`; nothing is offered that the answers don't
   hold, and every move builds exactly the JSON the builder would for the same answer (a test per kind of move). The
   browser still holds no rules: an answer it shows is one the engine listed, a shape's filling one the engine judges.
2. **One narrowing model for the board, the mats, the popovers and the panel.** Viper activation is not a special
   case: a click on a viper keeps its activations, the locations and the Cylon ships they name light up, and a menu
   next to the viper lists them. The same holds for a card with targets, a location action with choices, a choice
   whose options name space areas (the raider activation order) or players.
3. **Board targets only for one kind at a time.** When the moves left are of several event kinds (the Action step),
   the board lights only what starts a kind: the location of a location action (the "Act" chip, wherever a remote
   action allows it) and, on the mats, the playable cards. A kind's other targets light up once it is the only one
   left, or when it is the only kind that names board targets (the Movement step's destinations next to a
   "Movement:" card). Otherwise the Action step would light half the board.
4. **The intent is sent only when it still fits.** After `choose(option)` the next pause must be a decision for the
   same player whose answers cover the event; then it is submitted with the new token. Anything else (an earlier
   decision, a rejection, another player) drops it silently and the panel shows the input as usual.
5. **Pickers by the shape.** A `SkillCardPick`, `QuorumCardPick` or `ShipPick` is one widget: tiles to toggle, a
   counter "2 of 3" (or "1 to 3"), the strength so far against `minStrength`, numbers in click order when `ordered`.
   A draw's concrete color maps are one widget too: a count per color with + and −, held to what some answer still
   allows, sent when the counts match an answer.
6. **Hands are sorted for display and picking**: by `SkillCardColor` (Politics, Leadership, Tactics, Piloting,
   Engineering, Treachery), then value, highest first. The engine compares cards as a multiset, so the order sent
   never matters.
7. **The topic says no more than the log.** It is the queuing scope's event class and its details only when the log
   would show them (`Disclosure.isPublic`, a `SecretEvent` only to its actor). A secret input stays a plain `Waiting`
   for the others, which the table words as "someone may play a card": an open interrupt window and its answers are
   hidden from players without a card for it (`TurnProjection.interruptWindow`), so the roadmap's "how many have
   answered" isn't shown, because it would tell who holds cards. `topic` is a projection field: no `FORMAT_VERSION`
   change, `WireSchemaTest` and `npm run gen` pick it up.
8. **The log tells the story and keeps the detail.** Each scope is one line (actor and event, its art); its facts fold
   under it, open for the last scope only. A turn's entries group by step (`StepChanged`). "Mine" filters to the
   entries the viewer acts in or that name his character. A click on an entry flashes what it names on the board.
9. **Deltas and animations are read from two projections in a row**, never from the log: a resource, the jump track,
   the distance, the nukes, a ship's place, a die value, the cards in a hand, a damaged location. A snapshot (a
   reconnect, undo) animates nothing that it can't attribute: only a change from one game message to the next of the
   same table version counts. They always play.
10. **The reference names, it doesn't quote.** Labels stay names (`CLAUDE.md`). A rules reminder is a link to the
    rulebook page with the article number ("Rule 10.4.2, page 29"); a cheat sheet is a short summary in our own words
    with the article to read, and the Attack Table's numbers are the engine's thresholds. The rulebook PDFs ship with
    the app (open question 2 of the roadmap) as Vite assets.
11. **The builder stays** as "Advanced" under the answers and as the sandbox's tool, unchanged.

## Phases

Each phase ends with `./gradlew build` green and `CLAUDE.md` updated for what it changed.

### Phase 1: the answer model and the panel (C1)

- `input/answers.ts`: `moves(game)`, `narrow`, `groups` (by event kind, with the components that vary), labels and
  art per move, `send` (choose, submit, intent). `gameStore` gains `intent` and drops it on any other pause.
- `input/AnswerPanel.tsx`: the input panel's body. A choice's options as buttons or card tiles (a grid when they name
  cards, characters or locations), an option that asks a decision unfolds into its answers; a decision's answers as
  buttons grouped by kind (one group open at a time), `SelectCharacterEvent` as the standee gallery; the shape pickers
  and the draw counter (decision 5); "Back" and "Clear"; the builder under "Advanced".
- Sorted hands (decision 6) in the mats, the pickers and the answers.
- Tests: the moves of each kind of input build the codec's JSON, the intent fires only on a matching pause, the
  pickers count, the draw counter allows only what an answer allows, sorting. The e2e specs answer through the new
  panel.

### Phase 2: the board and the mats answer (C1)

- `BoardSvg`: targets for locations, ships (each ship of a pile) and standees from the narrowed moves, the "Act" chip
  for every location action among them, hover labels, a click narrows or sends; a popover next to the clicked element
  lists the moves left (`input/AnswerPopover.tsx`), viper activations included.
- `PlayersPanel`: the asked player's cards that start a move glow and click; a player target lights his mat.
- `boardMoves.ts` folds into the answer model; its test becomes the board's.
- Tests: targets per kind (move, location action, viper activation, raider area order, a player target), the popover's
  moves, a card play from the mat.

### Phase 3: what the game waits for (C2)

- Core: `GameEngine.pendingScope()` (the `ScopeOpened` of the pending frame's parent), `GameProjection.of` with it,
  `Topic(event, details)` on `Decide`, `Choose` and `WaitingFor`, hidden as decision 7 says. Backend: `GameSessions`
  passes it; `WireSchema`, `schema.json`, `types.ts`.
- Frontend: `pauseLabel` (who, what, and the topic), the input panel's heading, and "Your move" on the board's edge (a
  lit frame and a banner with the sentence) for the asked viewer.
- Tests: `GameProjectorTest` (a public topic, a secret scope hidden from others, the root), `WireSchemaTest`, labels.

### Phase 4: a log you can read (C2)

- `LogPanel`: steps within a turn, a story line per scope with its facts folded, "Mine", a click flashes the
  locations, ships and characters the entry names on the board (`table/flash.ts`).
- Card names in the log and the prompts hover to their image (labels as parts: text and art), C3's first item.
- Tests: grouping by step, the filter, the flash's targets, the parts.

### Phase 5: deltas and animations (C2)

- `table/changes.ts`: the differences between the last two projections of a table version (decision 9).
- Floating "−1 Morale" at the dial, the jump track, the distance and the nukes; a ship that moved glides (FLIP on
  `data-ship`), the die rolls, new cards slide into the hand, a newly damaged location flashes.
- Tests: the differences, and that a reconnect or undo snapshot shows none.

### Phase 6: reference without leaving the table (C3)

- The rulebook PDFs as Vite assets (copied by the build), `RuleLink` (article and page), and a reminder link per kind
  of input (`labels/rules.ts`, the articles looked up in the combined rulebook).
- A "Reference" drawer: every card by deck with a search box, the character sheets, the Loyalty rules, and the cheat
  sheets (turn sequence, Attack Table, skill check order, the Crisis Card icons). A mat's portrait opens its sheet.
- Tests: every reminder's article has a page, the search, the Attack Table matches the engine's thresholds.

### Phase 7: the exit, and docs

- e2e `playthrough.spec.ts`: a seeded four-player sandbox from setup to an ending through the answers only (the
  panel, the board, the mats), never opening "Advanced".
- `CLAUDE.md` (the frontend sections), `roadmap.md` (milestone C done), this plan's status.

## Open questions

None blocking. Where the rulebook is silent on how a cheat sheet should look, the summary is ours.

## Not in this plan

- Presence, chat, the results screen, notifications (milestone D).
- A setting to turn animations off (the roadmap says they always play).
- Drag and drop. Ordering is by numbered clicks.
- Mobile layouts beyond the existing narrow-screen fallback.
