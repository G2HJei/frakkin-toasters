# Frontend

`frontend/` is React 19 + TypeScript (strict) + Vite, built by Gradle through node-gradle (its own Node 24, `npm ci`):
`./gradlew build` type checks, builds and tests it, and the backend's `bootJar` packs `frontend/dist` under `static/`,
with `AppRoutesController` forwarding the app's routes to `index.html`. React Router, TanStack Query for REST (the lobby
lists poll), Zustand for the game. The table, the board and the art are in [frontend-table.md](frontend-table.md).

The browser holds no game rules: a picker only narrows to plausible answers, the engine judges them.

## Commands

`npm run dev` (Vite on 5173, proxying `/api`, `/ws`, `/oauth2` to 8080; the `dev` profile allows its origin), `npm test`
(Vitest), `npm run gen`, `npm run e2e` (Playwright, against the running app with the `dev` profile, `E2E_BASE_URL` to
point elsewhere).

## Wire schema

`./gradlew :backend:exportSchema` writes `frontend/src/wire/schema.json` (`backend/wire/WireSchema`), and `npm run gen`
turns it into `src/wire/types.ts`. `WireSchemaTest` fails when the checked-in schema is stale, and `tsc` when the UI uses
a field that went away. `GET /api/schema` serves the same JSON. It holds:

- every record with its components and type references, unions, enums, the socket messages;
- the `location` of a location's action, the `quorumCard` or `mutinyCard` a card's event plays, the `source` of an
  event (`@Source`) and the `sources` of an enum's constants (`GrantedBy`);
- the `colors` of `SkillCardType` (a hand lies by color, `table/skillCards.ts`), the `characters` of `CharacterAbility`
  (the sheet that prints each, its art) and its `miracles` (`isMiracle`), the `types` and `skillSets` of `Character`
  (the strip beside a mat's portrait), the `attackTable` (`AttackTable` in `core`, read from the attack results'
  thresholds, for the cheat sheet);
- a `role` for each int by its name (`ComponentRole` in `core`): `player`, `ship`, `scope` or `count`.

## Source layout

- `src/api/`: `http.ts` sends the `XSRF-TOKEN` cookie as `X-XSRF-TOKEN`, `queries.ts` the hooks. `devTab.ts`: a
  tab's own dev user (sessionStorage, set by `?as=<name>`, the login page's "This tab only" or the top bar's "Another
  user in this tab"), sent as `X-Dev-User` and the socket's `?devUser=`, and carried to the game tabs it opens
  (`withTabUser`); its "Log out" forgets it, leaving the browser's session alone.
- `src/live/`: `GameSocket` reconnects with backoff (at once after a `4001`, the seat changed), pings every 20 s and
  drops a connection that heard nothing for 50 s (a lost network), opening a new one at once; `gameStore` holds the
  game, its token, the log, the last rejection and the `intent` (an answer to submit once the option just chosen asks
  its decision); `chatStore` the chat's messages (each once, by id) and the last refusal, fed the `chat`, `chatLog` and
  `chatRejected` messages by `TablePage`.
- `src/labels/`: all wording, see [Labels](#labels).
- `src/input/`: the input panel, see [Answers](#answers). Under it the `AnytimePanel`: the game's `anytime` answers as
  moves (`anytimeMoves`) in the answers' `Groups` (e.g. the Motive Cards whose requirement is met), shown only while a
  player the viewer acts for has one, sent as listed.
- `src/table/`: the board and the panels around it ([frontend-table.md](frontend-table.md)).
- `src/sandbox/`: undo, export, restart with the seed, setup by injecting a `SandboxEvent` that `SetupForm` builds from
  the schema (exactly the codec's JSON), the inspector.
- `src/art/`: the images ([Art](frontend-table.md#art)). `src/pages/`: the website.

## Labels

`src/labels/` holds all wording, in plain sentences; only names, never a card's printed text. An answer is an order to
the player it names ("Choose the top option of Riots").

- A humanized fallback, and `overrides.ts` for names (an alternate sheet is named like its character, nowhere
  "alternate").
- `templates.ts`: a sentence per event and fact, the words of an answer on a button, of an event a player picked in a
  choice and of an event whose fields are hidden. Its template language has optional parts, plurals, words per constant,
  "the" in front of a location, a named constant, an icon alone, a die's face, and tone markers (`<+…>` good news for
  the fleet, `<-…>` bad news, e.g. a resource gained or lost, a skill check's result).
  - A location the words mention is a constant (`{Location:BRIG}`) so it shows its card; so is a Quorum Card
    (`{QuorumCard:PROBATION}`) and a type of Skill Cards (`{SkillCardColor:TREACHERY}`, shown in its color).
    `labels.test.ts` checks it, the words of `{a?b:c}` too.
  - `{field|damage}` words Galactica's damage tokens, `{field|count}` counts ships by kind ("2 vipers").
  - `{Holder:PRESIDENT}` names a title by the player who held it then (his portrait in front, the title's card on hover:
    `holderPart`, the log's `titleHolder`); a line about a change of the title names it by its insignia,
    `{Title:PRESIDENT}` (checked by `labels.test.ts`).
- `facts.ts`: a scope's facts as sentences (a card drawn into a hand "draws …", one lost to a discard pile "discards …", moves of a kind in a row and ships that moved alike are one sentence, bookkeeping left out).
- They fill in as `Part`s, which `art/Words.tsx` shows: a named card, character or location on hover (`artOf`:
  `partArt`); the icon in front of a resource (tinted like its words), a ship (by type, `shipType`), a title, a token,
  the die or a character's portrait (`iconOf`: `partIcon`, only the log passes it); a player by his portrait and his
  sheet on hover where the context knows his character (`playerPart`, the log's `characterOf`; a `Part` keeps the
  `player` it names); a type of Skill Cards, the count of hidden cards and a Skill Card deck's name in the deck's color
  (`skillColor`, `skill-type-*`: Politics yellow, Leadership green, Tactics purple, Piloting red, Engineering blue,
  Treachery brown; a named card stays a name to hover); a part's tone (green or red, in the log).
- `labels/pause.ts` words what the game waits for: the heading ("Laura Roslin decides", "Waiting for …") and a line from the
  decision and the input's `topic` ("Draw Skill Cards · Receive Skills step", "Crisis: Riots"), led by the card or
  ability the topic comes from when its words don't name it (`unnamedSource`: "Forward Thinker: Lee Adama may activate
  his location"). A secret input says only that someone may play an interrupt card.
- `results.ts`: the words of the results (the ending, each `Moment`, the statistics' names).
- `rules.ts`: see [Reference](frontend-table.md#reference). `board.ts`: the board's section names.

## Answers

`input/answers.ts` turns the pending input into `Move`s: one per concrete answer, shape (an example and a `Pick`) or
option of a choice, and an option that asks a decision first into one per answer of that decision.

- `targetsOf(move)` lists what a move names (its kind, the location of a location action, locations, ships, other
  players, held cards, nested records too). `narrow` keeps the moves that name every pick of the `selection` store
  (`input/selection.ts`, cleared by each game message; "Back" and "Clear").
- `plan` says what to send: an answer is submitted, an option chosen, and an option with an answer of its decision is
  chosen with the answer as the `intent`. `TablePage` submits the intent on the next game only when `intentDue` (a later
  game, the same player, a decision whose answers `cover` it), then keeps it as `sent` until the game after (the input
  panel is `aria-busy` meanwhile). An update with an `automatic` scope of its player drops it: the server took that only
  answer itself.
- `AnswerPanel` shows a choice's options in order, an option's decision as a section of its answers (the first one
  open), the answers grouped by kind (`groupMoves`; a group's moves differ in `varyingComponents`, worded by
  `moveLabels.ts`). The characters to choose (`SelectCharacterEvent`) lie by pool, side by side as large as their
  portraits: military leader, political leader, pilot, support, Cylon Leader. The Pass is last with its icon (a
  `NoOpEvent`, or an answer that names none of what its group's others do, e.g. no interrupt card; its group then names
  the answer left by what told them apart).
- Every other answer is a row as high as a card, its images in a slot as wide on every row (`ArtSlot`).
  `input/answerArt.ts` picks them: what the answer names (`recordArt` with a `ValueArt`: a player as his portrait, a
  title as its insignia with the card as preview, an ability as its character's portrait, a deck as its back, a Skill
  Card color as a back in it, a die result as the face, a resource as the board's icon (`resourceIconArt`), ships by id,
  no location since the board shows it), the event's `@Source`, what its kind does where its fields don't say
  (`KIND_ART`: the deck a draw takes from, the Skill Card back for a random discard or the Destiny deck, the title a
  Title Card action is, the Destination back on each destination, whose cards the choice shows once above its options
  (`choiceArt`; a Crisis Card stays on each option), the Movement step's icon on a move and on the "Movement:" answers:
  `stepIconArt` draws `StepIcon`'s markup), the back of a shape's cards (the Skill Card back for cards into a check or
  to discard), and the miracle token last on every answer of a miracle ability. Neither the token nor a step's icon
  moves to the group's name (`staysOnEachAnswer`).
- A draw is a card per color that a click takes one more of, back to what every answer draws once it has the most an
  answer leaves it. What every answer draws is picked from the start (e.g. a skill set's one-color options), a color
  every answer draws as many of comes first and fixed, a color left no room is disabled in place, one no answer has isn't
  shown.
- A shape is a picker: sorted cards, a counter, the strength against `minStrength`, the click order when ordered; the
  decision's constraints above it.
- A group whose answers are a product of what they differ in (`input/parts.ts`: more ways than its largest part has
  values, e.g. the "Engine Room"'s two cards, "Make a Deal"'s target, destination and drawer, every pair of damage
  tokens) is picked part by part (`PartsWidget`): a row of values per part (a list's elements, or numbered siblings such
  as `discardCard1`/`discardCard2` as one pool in any order), each open while an answer still fits, and the answer they
  name in full is sent as the engine listed it; a pair in either order is one answer.
- The asked viewer's board gets a red edge. The board and the mats answer too
  ([Board answers](frontend-table.md#board-answers)).

## Website

`GamesPage` (the lobby: my sandboxes and games, open lobbies, games to watch with their vacant seats, hosting),
`RoomPage` (seats and observers with a dot for who is there, join, leave, start; the host removes or bans a user and
cancels the game),
`SettingsPage`. The table page shows why it doesn't open a game: saved by another version, cancelled by the host, or a
running game the user isn't in (a finished one anyone may watch). A 401 goes to the login page. The lobby's pages and a room have the top
bar (`TopNav`), the table doesn't. A game opens in a tab of its own: the links to it, and hosting, joining, a new or
imported sandbox go through `openGameTab` (`src/pages/gameTab.ts`), which opens the tab on the click, while the browser
allows it, and points it at the game when the request answers. A sandbox's "Restart with this seed" stays in its tab.

## e2e

`frontend/e2e/`: each spec follows a game into its tab (`tabs.ts`).

- `sandbox.spec.ts` plays a seeded sandbox through a turn (reading the stacked Crisis Card from the Crisis deck's
  hover, Starbuck's "Secret Destiny" letting it resolve; the seed seats the first player), undoes, exports and imports.
- `lobby.spec.ts`: five users host, join, observe, leave and start a game; nobody sees another player's cards, an
  observer gets no input, two users race for the last seat.
- `sessions.spec.ts`: four users and a session: who is there in the room and on the mats, the chat with a player's and
  an observer's message, a player who goes away, a vote and the host's hand-over of the seat to the observer, who sees it
  as hers, and the host's abandon, which shows everyone the results.
- `playthrough.spec.ts` plays a seeded four-player sandbox (`PLAYTHROUGH_SEED`, 7 by default) to an ending through the
  answers alone, picked at random from a seeded generator (options, answers by kind, counters, parts, pickers), so an
  input without answers fails it. It takes minutes.
