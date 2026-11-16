# Frontend plan

## Context

Roadmap step 5: a web app to play Frakkin Toasters in the browser. It has a website (login, lobby, the games you are
in, games to watch, game rooms) and the table, where a game is played over the web socket of `backend-plan.md`. The
first milestone is a **sandbox**. One user plays every seat, nothing is hidden, and there are tools to test the engine
by hand: undo, a seed, export and import of the journal, and scenario setup.

This plan starts when `backend-plan.md` is done (all 9 phases). It relies on these parts of it:

- the JSON codec and its closed type registry (phase 2)
- `GameProjection` and `LiveUpdateProjection` with their `Viewer` (phases 3 and 4)
- `GameSessions` (phase 5)
- the logins, with the `dev` profile (phase 6)
- the lobby REST (phase 7)
- the `/ws/games/{id}` socket and its six message types (phase 8)

The sandbox needs a few additions in `core` and `backend`. Phases 1 and 2 add them on top of the finished backend, so
`backend-plan.md` stays as it is.

Every phase ends with the tests green. The plan is done when:

- a user can start a seeded sandbox, play every seat through a full turn, undo, export the journal and import it again;
- three players and an observer can log in, host, join and play a game on the board through the UI.

## Target design

```text
+------------------------------------- browser ---------------------------------------+
|  pages:  Login | Lobby | Room | Table | Settings | New sandbox                        |
|                                                                                     |
|  TanStack Query  ---- REST ---->  /api/me  /api/games  /api/sandbox  /api/schema    |
|  (polls the lobby)                                                                  |
|                                                                                     |
|  Table                                                                              |
|   board | players | turn | skill check | log   <-- gameStore <--+                   |
|   input panel: choice options | event builder ---> GameSocket --+-- ws --> /ws/games/{id}
|   sandbox toolbar: undo | setup | seed | export | inspector                         |
|                                                                                     |
|  wire/schema.json + types.ts (generated from the backend)    labels/ (all wording)  |
+-------------------------------------------------------------------------------------+
```

- **gameStore** holds the latest `game` (token and `GameProjection`), the log (the scopes of `log` and `update`),
  the connection state and the last `rejected`.
- **Input panel.** A choice is one button per option, in order, answered by index. A decision is the event builder:
  a form generated from the schema for the action the decision asks for.
- **The table** renders only what the projection holds. It never works out hidden information and never decides
  legality.

## Decisions

1. **React 19 + TypeScript (strict) + Vite, in a `frontend/` Gradle project.**
   - The node-gradle plugin runs `npm ci`, `npm run build` and `npm test`, so `./gradlew build` builds and tests it.
   - The backend jar serves the built app from `static/`, and a fallback forwards app routes to `index.html`. That
     gives one deployable, as backend decision 5 has one instance.
   - Libraries: React Router, TanStack Query for REST, Zustand for the game store. Vitest and Testing Library for
     unit tests, Playwright for end to end. No UI kit.
2. **The browser holds no game rules.** The engine is the only judge (backend: "Not in this plan: the legal
   answers"). The UI may narrow a picker to plausible candidates, e.g. the cards in the hand or the ships on the board.
   Then it submits and shows the engine's message when the answer is rejected.
3. **One schema, exported by the backend, is the source of the TypeScript types and of the event builder.** Nothing
   copies a Java record by hand. A backend test fails when the checked-in schema is stale, and `tsc` fails when the
   UI uses a field that went away.
4. **Every decision can be answered from day one** through the generic event builder. Playing on the board (phase 8)
   replaces it one input at a time. Each board interaction must build the same event the builder would build, and the
   builder stays as "Advanced".
5. **The frontend owns the wording**, as backend decision 7 says. `labels/` gives a name for every enum constant and
   a template for every event, fact and option. A missing name falls back to the humanized constant.
6. **The look is our own.** The boards are schematic SVG and the cards are text tiles. The scans in
   `materials/images` are the publisher's art: reference for the layout, not assets to ship. The UI shows card and
   character names, not their printed text.
7. **The sandbox is a game mode, not a separate app.** It uses the same table and socket, with a `Sandbox` viewer
   that sees everything and answers for whoever is asked. It is stored like any game, so it survives a restart, shows
   in the lobby and can be exported.
8. **A seed reproduces a sandbox.** The same seed and the same inputs give the same game, also after a restore or an
   undo.
9. **Undo, setup and export exist only in sandbox games.** The backend refuses them for any other game.
10. **The lobby polls and the table listens.** The backend's socket is per game, so lobby lists refetch every few
    seconds. Logins are full-page redirects to Spring's endpoints, and REST calls send the CSRF token from the
    `XSRF-TOKEN` cookie.

## Phase 1 - The sandbox in `core`

- **`Viewer.Sandbox`** in `projection/`. It sees everything: what everyone sees once the game has ended, plus the
  draw piles in order. In `GameProjection` it gets the pending input in full, even a secret one. In
  `LiveUpdateProjection` every scope and fact shows as recorded.
- **Determinism (decision 8).** `Randomness.reseed(long)`, and `GameEngine.start(playerCount, seed)` and
  `restore(journal, seed)`. An engine with a seed reseeds before each input, from the seed mixed with the journal
  size. That works because a restored engine gets a fresh unseeded `Randomness` today (`GameReplayer.replay`), so a
  plain seed reproduces a game only until the first restore. Games started with a `Randomness` behave as before.
- **Undo.** `GameEngine.undo()` goes back to the pause before the last input.
  - Rebuilding reuses the rollback path: `rebuild` from a prefix of the journal (`GameEngine.java:158`).
  - The engine knows where each input began. Live, that is the `entriesBefore` of `advance`. After a restore,
    `AgendaReplayer` finds them:
    - the `ScopeOpened` that answers a decision frame (`runs`, `AgendaReplayer.java:66`)
    - every `ChoiceMade` (`select`)
    - every `EventInjected`
  - Undoing a choice that led to a decision undoes only the choice. A finished game can be undone. Before the first
    pause there is nothing to undo, so it throws `InvalidActionException`.
- **Scenario setup.**
  - `event/sandbox/` holds a `SandboxEvent` marker and a starter set of plain `Event`s with processors:
    - put a chosen Crisis, Destination, Skill or Destiny card on top of its deck
    - give a player a Skill Card
    - move a character
    - place a Cylon ship in a space area
    - raise or lower a resource
    - set the jump preparation track
    - damage a location
    - assign a title
  - Each kind of card has its own event with typed fields, so the event builder can build it. More events come when
    testing needs them.
  - A processor calls the ops the rules use where one fits (e.g. `GalacticaOps.placeRaider`,
    `AbstractOps.takeFromSupply`). `operation/SandboxOps` covers only what no rule does. A new
    `DeckFact.CardsPulled(deck, cards)` takes a stacked or given card out of its draw pile, so no copy appears. It is
    handled in `FactApplier`, and in `LiveUpdateProjection` it shows only a count.
  - `GameEngine.inject(SandboxEvent)` runs the event at once, ahead of the pending input, and the same run loop does
    the win checks. A new `JournalEntry.EventInjected(event)` before its scope tells `AgendaReplayer` to push the
    event. `submit` never takes a sandbox event.
  - A pending choice keeps the options it was built with, even when a setup makes them stale. That's acceptable in a
    sandbox.
- The test-only `StackDeckEvent`, `GiveSkillCardEvent`, `PlaceRaiderEvent` and `TestOps` show the shape. They stay in
  the tests.
- Tests:
  - A seeded game undone at every pause gives the `GameSnapshot` that pause had.
  - The same seed and the same inputs, with a restore in the middle, give the same snapshot.
  - Each setup event, injected and then restored, gives the same snapshot.
  - Undo after an injected event removes it.
  - `FactApplierTest` covers `CardsPulled`.
  - A `Sandbox` projection holds every hidden item of phase 3's table.

## Phase 2 - The sandbox in `backend`

- **Schema.** Flyway `V2` adds `game_bundle.mode` (`STANDARD`, `SANDBOX`) and `game_bundle.seed` (`bigint`, null for
  a standard game). The new events and the `EventInjected` entry go into the codec, so the format lock asks for
  `FORMAT_VERSION = 2`. No games need keeping yet.
- **Start.** `POST /api/sandbox {playerCount, seed?}` creates a `RUNNING` sandbox with `GameEngine.start(n, seed)`,
  and the seed is random when none is given. The caller is the only participant, with no `player_number`. The mode
  makes the connection's viewer `Sandbox`. Nobody else can join or observe.
- **Commands.**
  - A sandbox connection answers for the player of the pending input. The server sets the player and the event's
    `playerNumber` from it (backend decision 9), so the client never names a player.
  - New client messages, all with the token:
    - `undo`: deletes the journal rows after the new sequence, in the transaction that updates the summary
    - `inject`: carries a sandbox event, decoded only if its type is a `SandboxEvent`
  - Both are `rejected` in a standard game.
- **History.** For every game, not only sandboxes: a client message `history` and a server message `log`, which is
  the `LiveUpdateProjection` of the whole journal for the viewer. A new connection gets `game` and then `log`. After
  an undo, every connection gets both, and the client replaces its log.
- **Export and import.**
  - `GET /api/games/{id}/journal`, sandbox only: `{formatVersion, seed, playerCount, entries}` through the
    `JournalCodec`.
  - `POST /api/sandbox/import` stores the file as a new sandbox once `GameEngine.restore(journal, seed)` accepts it.
    It has a size limit and refuses another `formatVersion` with a message.
- **Regression journals.** `backend/src/test/resources/journals/`, with a helper that loads a file into a
  `GameEngine`. A test checks that every file there restores. A bug found in the sandbox becomes a test: export the
  journal, drop it there and write the asserts.
- Tests, over the socket against the running app:
  - One client plays a seeded sandbox through character selection and a turn, answering every seat.
  - Undo and then the same input give the same `game`.
  - Export, import, then the same projection.
  - Inject during a secret interrupt window.
  - `undo`, `inject` and export are refused in a standard game.
  - A reconnect gets `game` and then `log`.

## Phase 3 - The `frontend` module and the wire types

- **Scaffolding.**
  - `settings.gradle` includes `frontend`, a Vite React TS project built through the node-gradle plugin (decision 1).
  - `backend`'s `bootJar` packs `frontend/dist` under `static/`, with a controller forwarding the app routes to
    `index.html`.
  - In dev, `npm run dev` proxies `/api`, `/ws` (`ws: true`), `/oauth2` and `/login` to `localhost:8080`, and the
    `dev` profile allows the Vite origin in the socket's `Origin` check.
- **The wire schema (decision 3).**
  - `:backend:exportSchema` writes `frontend/src/wire/schema.json`. It walks the codec's registry with the
    record-component walker of the format lock (backend phase 2), and lists:
    - every event: its name, its interfaces (`PlayerEvent`, `ActionEvent`, `LocationEvent`, `SecretEvent`,
      `ConstrainedEvent`, `SandboxEvent`), its components with type descriptors, and the `location()` of a
      `LocationEvent`
    - every fact and journal entry
    - every enum and its constants
    - the records and sealed unions of the projections and the socket messages
  - An `int` or `Integer` component gets a role from its name: `player` (`playerNumber`, `targetPlayerNumber`, ...),
    `ship` (`raiderId`, `humanFighterId`, ...) or `count`. A test fails on a name it can't place, like backend phase
    4's card-component test.
  - A backend test fails when the checked-in `schema.json` differs from the export.
  - `npm run gen` (`scripts/gen-types.mjs`, no codegen dependency) writes `src/wire/types.ts`, with sealed types as
    unions discriminated by the codec's `type`.
  - `GET /api/schema` serves the same JSON. A test checks that the copy built into the app matches it.
- **Plumbing.**
  - `api/http.ts`: `fetch` with credentials and the `X-XSRF-TOKEN` header, plus TanStack Query hooks for `/api/me`
    and the sandbox calls.
  - `live/GameSocket.ts`: one socket per open table, reconnecting with backoff. On `rejected` stale the server has
    already sent the current `game`.
  - `live/gameStore.ts`: Zustand, fed by `game`, `log` and `update`. Commands always carry the token of the latest
    `game`.
- **`labels/`.** Enum names, plus templates for events, facts and options. Hand-written overrides sit on top of the
  humanized fallback, and a test checks that every override names a constant or type in `schema.json`.
- Tests:
  - The store against a fake socket: order of messages, reconnect, stale rejection.
  - `gen-types` on a small sample schema.
  - `./gradlew build` builds the jar with the app inside.

```text
frontend/
  build.gradle  package.json  vite.config.ts  tsconfig.json  scripts/gen-types.mjs
  src/  main.tsx  App.tsx
        api/  wire/  live/  labels/  pages/
        table/{board,players,turn,skillcheck,log}/  input/{widgets}/  sandbox/
  e2e/  (Playwright)
```

## Phase 4 - The sandbox table (the initial version)

Playable by hand, before there is a board. The game shows as panels of plain HTML.

- **Pages.**
  - Login: the provider buttons, and in `dev` the login by name.
  - New sandbox: player count 3 to 7, an optional seed, and import a journal file.
  - Table at `/games/:id`.
- **State panels.** Everything the `Sandbox` projection holds:
  - players: character, location, titles, hand, Loyalty/Motive/Mutiny/Super Crisis cards, miracle token, revealed
    Cylon, Mutineer, seen Loyalty Cards
  - resources and tracks: jump preparation, boarding party, pursuit, distance and destinations, nukes
  - each location with its characters, and each space area with its ships (kind, id, pilot), on Galactica and on the
    Cylon Fleet board
  - damage, decks (counts, discard piles and the draw piles in order)
  - the turn: number, current and acting player, step, the Crisis Cards of the turn and those kept in play, the die
    roll and its cards, the interrupt window and its cards
  - the skill check: type, target, played-before cards, piles, strength, result
- **Input panel.** It answers for the asked player and names them (number and character).
  - **Choice.** One button per option, labeled by `labels/` from the option's tree:
    - `NoOpEvent` is "Pass"
    - a `PlayerDecisionEvent` option is the action it asks for
    - an `AllOf` joins its parts with "then"
    - otherwise the event's template with its field values
  - **Event builder.**
    - The decision's `action` comes from the schema. An interface (e.g. `ActionEvent`) first picks the concrete
      event, the action of the player's location first.
    - One widget per component:
      - player picker; ship picker (the ships of the projection, by kind and area); number; checkbox
      - enum select
      - `SkillCard` and `List<SkillCard>`: picked from the hand, by position, because equal cards exist
      - a count per `SkillCardColor`
      - the nested `ViperActivation` form (launch, move, attack or escort)
      - the tiebreak lists, picked from the conflicting played cards
      - "none" for a nullable field
    - `playerNumber` and constraint fields are hidden, because the server fills them in. The constraint is shown as
      text.
  - **Rejected.** The engine's message stays next to the input until the next `game`.
- **Sandbox toolbar.**
  - Undo, Export (downloads the journal), the seed with "Restart with this seed".
  - Setup: every `SandboxEvent` in the same event builder, sent as `inject`.
  - Inspector: the raw JSON of the projection and the pending input.
- **Log.** Every scope with its actor, event and facts, from `log` and `update`, grouped by turn and collapsible. Fact
  templates are written by hand for the common facts (moves, cards gained and lost, resources, damage, ships moved,
  die rolls). The rest are `FactName field=value`.
- Tests:
  - Vitest: the event builder per widget, and on events of each shape it builds exactly the codec's JSON; the option
    labeler on the shapes listed in "Choice".
  - Playwright against the running app (`dev` profile): start a sandbox with seed 42, select characters for every
    seat, play a full turn, undo, export, import, and inject a stacked Crisis Card that the next Crisis step draws.

## Phase 5 - The board

- SVG components, laid out in `table/board/layout.ts`:
  - Galactica's locations, with the 6 space areas around the ship in clock order
  - Colonial One, the Cylon locations, Pegasus
  - the Cylon Fleet board with its 6 spaces and the pursuit track
  - the resource dials, jump preparation and boarding party tracks
  - decks (counts, top of the discard pile), destinations and distance, titles, nukes
- Character tokens sit at their location. Ship tokens show kind, id and pilot, and a damaged location is marked.
- The player panels, the turn bar and the skill check panel of phase 4 stay beside the board. The plain-HTML board
  panels go away.
- A test checks that every `Location` in `schema.json` has a position, so no location can go missing from the board.
- Tests:
  - Component tests render projections taken from the phase 4 e2e run: each character and ship is drawn where the
    projection puts it.
  - A Playwright screenshot of the board after setup, for review, not compared.

## Phase 6 - The website: lobby and game rooms

- **Lobby** (`/games`):
  - open lobbies, to join as a player or an observer
  - my games, with a "your move" badge from `waiting_for_player_number`
  - running games to watch (the lobby list of backend phase 7 gets `status=RUNNING`, the one backend addition here)
  - my sandboxes
  - host a game (name)
  - It refetches every few seconds.
- **Room** (`/games/:id` while `LOBBY`): seats 1 to 7 and the observers, with join, leave and switching between seat
  and observer. The host starts it with 3 to 7 players. When the status turns `RUNNING`, the page becomes the table.
- **Settings** (`/settings`): the name, through `PUT /api/me`.
- **Session.** A 401 from `/api/me` goes to the login page, and a refused socket handshake shows why.
- Tests: Playwright with three browser contexts and `dev` logins: host, join, observe, leave, start, the race for the
  last seat as the UI shows it, and a running game appearing under "watch".

## Phase 7 - The table for players and observers

- The same table with the viewer the server gives: player n or observer. Hidden things show as the projection holds
  them:
  - the other players' hands as counts per color (backs)
  - Loyalty and other cards as counts plus the revealed ones
  - played cards as counts until they are revealed
- **Pending input.**
  - The asked player gets the input panel.
  - The others see "waiting for (player)", or a plain "waiting" while it is secret. Nothing hints at who is asked.
  - Observers never get an input panel.
- **Your move.** The tab title and favicon mark an input for you. No browser notifications (backend: "Not in this
  plan").
- **Finished games.** The winning team and the winners, then everything revealed, as the projection gives it.
- Tests:
  - Playwright: three players and an observer play the scripted turns of a seeded game through the UI.
  - During an interrupt window, the others' DOM doesn't change.
  - No player's store or DOM holds another player's hand. The server already guarantees it, and the test checks that
    the UI asks for nothing more.

## Phase 8 - Playing on the board

The frequent inputs get direct interactions (decision 4), each building the same event the builder builds:

- Movement: click a location, and pick the discard card when the move goes between ships.
- Location action: an "Act" button on your location, with its remaining fields inline.
- Drawing Skill Cards: color counters bound by the constraint.
- Playing cards before or into a skill check, and discarding: select in the hand, with the count from the
  constraint.
- Viper activation: click a viper, then a space area or a target.
- Choices whose options name a target: the targets light up on the board (characters for "Sickbay" and "Brig",
  ships, space areas), and the two sections show as buttons on the Crisis Card.

Everything else stays on the builder, under "Advanced". Tests: per interaction, the event it builds equals the
builder's for the same answer.

## Phase 9 - Docs

- `CLAUDE.md`: the `frontend` module, its packages and commands (`npm run dev`, `npm test`, `npm run gen`,
  `npm run e2e`), the wire schema and its regeneration, the sandbox (`event/sandbox/`, `inject`, `undo`, the seed),
  the regression journals, and roadmap step 5 done. Each earlier phase already fixes the lines it makes stale.
- `README.md`: the frontend next to the backend, how to run it locally, and how to turn a sandbox journal into a
  test.

## Critical files and reuse

- **New:**
  - `frontend/**`
  - `core/.../event/sandbox/**`
  - `core/.../operation/SandboxOps.java`
  - `backend/.../sandbox/**`
  - `backend/src/main/resources/db/migration/V2__sandbox.sql`
  - the schema exporter in `backend`
- **Changed:**
  - `core/.../engine/GameEngine.java` (seed, `undo`, `inject`)
  - `engine/AgendaReplayer.java` (input starts, `EventInjected`)
  - `engine/journal/JournalEntry.java`
  - `engine/journal/fact/DeckFact.java` and `FactApplier.java` (`CardsPulled`)
  - `state/Randomness.java` (`reseed`)
  - `projection/` (`Viewer.Sandbox`)
  - the backend's `JournalCodec` (`FORMAT_VERSION = 2`), socket handler and lobby list
  - `settings.gradle`
- **Reused:**
  - `GameEngine.rebuild` for undo
  - `AgendaReplayer.runs` and `select` to find the inputs
  - `Randomness.seeded`
  - `GalacticaOps.placeRaider` and `AbstractOps.takeFromSupply` for setup
  - the test-only setup events as the model
  - the format lock's record walker for the schema
  - `GameSnapshot` for every determinism test

## Verification

- `./gradlew build`: `core`, `backend` (Testcontainers) and `frontend` (Vitest) are green, and the jar holds the app.
- By hand, locally:
  1. `docker compose up -d`
  2. `./gradlew :backend:bootRun --args='--spring.profiles.active=dev'`
  3. `cd frontend && npm run dev`
  4. Open `http://localhost:5173`, log in by name, start a sandbox with 4 players and seed 42.
  5. Select every character, play a turn, undo, redo the same input and see the same result.
  6. Export, import, and stack a Crisis Card with Setup.
- `npm run e2e` runs the Playwright suites of phases 4, 6 and 7 against that app. During implementation the
  Playwright MCP tools can drive the same flows for a look.

## Open questions

1. **The draw piles in order** are visible to the `Sandbox` viewer. I read "nothing is hidden" as including them.
   They are useful for testing what comes next.
2. **CSRF through the `XSRF-TOKEN` cookie** (decision 10) assumes backend phase 6 uses Spring's cookie token
   repository. If it doesn't, `GET /api/csrf` hands the token out instead.
3. **The full `log` on every connect** is simple, but a long game sends a lot of text. Paging by turn can come when
   it hurts.

## Not in this plan

- Legal-move hints: a list in `core` of the legal answers to a decision. The UI tries and shows the engine's message.
  It would be its own plan, and phase 8 would use it.
- The printed text of cards and characters, and art from the scans (decision 6).
- The events still open in the engine (Executive Order, Quorum and Super Crisis cards, Cylon reveal). Their decisions
  show in the builder once they exist. Until then, picking an option whose event has no processor (e.g.
  `MoveCivilianShipEvent`) comes back as a server failure, which is what the sandbox is there to find.
- Exporting standard games, chat, browser notifications, bots, phone layouts and languages other than English.
