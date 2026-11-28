# Backend

`backend` (Spring Boot 4, Java 21, package `xyz.zlatanov.frakkintoasters.backend`) runs one `GameEngine` per game,
stores its journal in Postgres and pushes to every viewer what he may see. It adds no game rules.

- `journal/` - `JournalCodec` (a `JournalEntry` to JSON and back, `FORMAT_VERSION`), `ValueCodec` (records by their
  components; a value of an interface type carries its `kind`, from the closed `TypeRegistry`; a `List<?>` of cards
  takes its type from the `DeckId` or `PlayerPile` beside it, `cardType()`), `WireCodec` (the projections), and
  `TypeWalker` (every type and its components, for the format lock). `FormatLockTest` checks the format against
  `src/test/resources/journal-format.txt`: a change needs a new `FORMAT_VERSION`, then run it with
  `-DupdateFormatLock=true`. An event is named by its simple name, a fact with its area (`DeckFact.CardsDrawn`).
- `game/` - `GameSessions` runs the commands of a game one at a time (a lock per game): session from the Caffeine
  cache or restored from the rows, the state token (`StateTokens`, the sequence hashed with `frakkin.token-secret`),
  the engine, the new entries and the `game_bundle` summary in one transaction (a failed write evicts the session),
  then the projections (with the engine's `pendingScope()`) to the `ProjectionSink`, under the lock. A viewer for whom nothing visible changed gets nothing.
  Every engine it starts or restores takes only answers while `frakkin.take-only-answers` is on (the default, see
  [Engine](engine.md#engine)). `JournalStore`, `GameBundles` (with `mode` and `seed`: a seeded game restores with its seed).
- `user/` - logins through Google and Discord (`LoginProviders`, only those with a client id), the `dev` profile's
  login by name (`POST /api/dev/login`, refused together with a real provider), `AppUserAuthentication` in the HTTP
  session, which Spring Session keeps in Postgres so a login survives a restart, and which lasts 30 days on the device
  (`spring.session.timeout` and `server.servlet.session.cookie.max-age`; MockMvc's mock servlet context doesn't apply
  the cookie's settings, so `LoginCookieTest` checks them on a real server; a provider's login lands on `/play`),
  `GET`/`PUT /api/me`, `GET /api/users` (every user's id and name, by name). CSRF through the
  `XSRF-TOKEN` cookie and the `X-XSRF-TOKEN` header.
- Dev simulation (`dev` profile only, refused with a real provider like the dev login): `DevTabLogin` makes a request
  with `X-Dev-User: <name>` (or `?devUser=<name>`, for the socket's handshake) that dev user's for itself alone,
  nothing in the HTTP session, so one browser plays several users side by side. It sits in the security chain after
  the session's context is loaded; its servlet registration is off. To play a game of several people locally: start
  the app with the `dev` profile, log in in one tab, and in each other tab open `/?as=<name>` (or "Another user in
  this tab" in the user menu, or "This tab only" on the login page); incognito windows and other browsers work with the
  plain dev login too (Chrome's incognito windows share one cookie jar between them).
- `lobby/` - `/api/games`: host, list (open lobbies, mine, running ones to watch; a cancelled lobby in none), join as a
  player (next free seat, 7 at most) or an observer, leave, and start by the host with 3 to 7 players (the seats close
  up to 1..n). The host's tools: `kick` (`{userId, ban}`: out of the lobby, or out of a running game and its seat; a
  ban in `game_ban` keeps him out), `cancel` a lobby (`FINISHED`, `end_reason = 'CANCELLED'`), `rematch` a finished
  game (a new lobby named "Rematch: …" with the final holders in the same seats, `rematch_id` on the old one).
  - Seats of a running game (`Seats`, `V4__seats.sql`): a seat is the `participant` row with its `player_number`, and
    `game_bundle.player_count` the seats; a seat nobody holds is vacant and the game waits there. `seat_holder` keeps
    who held each seat, since and until when, and why he left (`LEFT`, `VOTED_OUT`, `KICKED`, `BANNED`). A player who
    `leave`s keeps watching as an observer. Anyone not banned takes a vacant seat (`POST /{id}/seat {playerNumber}`),
    but a user who held a seat of the game may only take that one back.
  - Votes (`POST /{id}/votes {kind, playerNumber}`, `/votes/withdraw`): `VACATE` a seat needs more than half of the
    other seated players, `ABANDON` more than half of all of them; only seated players vote, and the host's vote passes
    at once (his "host action"). The room lists the open votes with the seated voters and what they need.
  - A change commits first, then `GameSessions` acts: an abandon (under the game's lock, which mustn't wait for the
    row the transaction locked) and `reconnect`: the connections of the users whose seat changed close with `4001`
    (`ConnectionPool.SEAT_CHANGED`) and the client reconnects at once, getting the viewer of his new seat.
  - `Presence`: who is there, in memory. In a lobby a user is online while his room page polls (`GET /api/games/{id}`,
    within 10 s); once the game runs, while he has a connection to it (`ProjectionSink.connected`). The room's
    `online` lists them.
  - `GameSessions.abandon`: `FINISHED` as `ABANDONED`, the session `closed()` (also once restored from the rows), so it
    refuses every command and projects no input and no `anytime`. The engine stays as it was: nothing is revealed.
- `live/` - `/ws/games/{id}`: the handshake needs a participant, or any user but a banned one for a finished standard
  game (watched as an observer: the table's URL is a read-only link), and an allowed `Origin`, and gives the connection
  its `Viewer`. `ConnectionPool` is the `ProjectionSink`; each `LiveConnection` sends from its own queue. Client messages:
  `ping` (answered with `pong`; the client pings every 20 s, and `ConnectionPool.closeSilent` closes a connection
  silent for `frakkin.socket-silence`, 60 s, so its user shows as away), `status`, `history`, `submit` (token, event: an answer to the pending decision, or an `AnytimeEvent`, which
  `GameSessions` runs ahead of it, with the player of the connection, the one it names in a sandbox), `choose` (token,
  option index). Server messages: `game` (token,
  projection), `update`, `log`, `report` (the `GameReport` of an ended game: on a new connection after the log, and to
  everyone with the command whose entries hold `GameEnded`; `GameSession.report()` reads it once per sequence, so an
  undo takes it back), `rejected`, `pong`, `chat`, `chatLog`, `chatRejected`. A new connection gets `game`, then `log`
  (then `report`), then `chatLog`. A game that can't be opened
  (e.g. saved by another `FORMAT_VERSION`) gets `rejected` (`UNAVAILABLE`, with the reason) and then the close 1008
  (`LiveConnection.closeAfterSending`), after which `GameSocket` doesn't reconnect; the table shows the reason, and the
  room's `unavailableReason` before any socket.
- `chat/` - `Chat`: the public chat of a standard game, in `chat_message` (`V5__chat.sql`), apart from the journal.
  One chat for everyone at the table: the players and observers write and read it, whoever watches a finished game
  without being in it only reads. A message carries the seat its writer held then (none for an observer). At most 500
  characters, 5 messages per 10 s per user and game (in memory). The socket's `say` (`text`) posts one and
  `ConnectionPool.broadcast`s `chat` to every connection of the game; a refusal goes back as `chatRejected`. A new
  connection gets `chatLog` (the last 300) after the log, read once it is in the pool, so a message written meanwhile
  comes twice and the client keeps it once. A sandbox has no chat. `LobbyChat`: the play page's chat, for everyone
  logged in, in memory only (the last 64 messages, gone with a restart): `GET /api/lobby/chat` lists them oldest first,
  `POST /api/lobby/chat {text}` posts one under the user's name, with the same length and rate limits. No socket: the
  page polls.
- `problem/` - `ProblemReports`: `POST /api/games/{id}/problems {text}` ("Report a problem" on the table) stores the
  text (1 to 1000 characters), the game, the user and the journal's last sequence in `problem_report`
  (`V6__problem_report.sql`); whoever can open the table may report, 20 times per game at most.
- `sandbox/` - `POST /api/sandbox {playerCount, seed?}` starts a sandbox (`game_bundle.mode = SANDBOX`, a seeded
  engine, the caller its only participant, with the `Sandbox` viewer, who answers for whoever is asked). The socket
  takes `undo` (deletes the rows after the new sequence; every connection gets `game` and `log` again) and `inject` (a
  `SandboxEvent`) only in a sandbox. `GET /api/games/{id}/journal` exports a sandbox (`formatVersion`, `seed`,
  `playerCount`, `entries`) and `POST /api/sandbox/import` stores one as a new sandbox once the engine restores it.
- Regression journals: `src/test/resources/journals/*.json` are sandbox exports, and `RegressionJournalsTest` checks
  that each restores. A bug found in the sandbox becomes a test: export the journal, drop it there, load it with
  `RegressionJournals.engine(name)` and assert. A fuzzer finding becomes one with `./gradlew :backend:test --tests
  '*FuzzJournalExportTest' -DfuzzJournal=4:117` (`fuzz-4p-117.json`): the journal at the pause before the finding, the
  `input` that failed there (`player`, an `option` and/or an `event`) and what it has to do once fixed (`expect`:
  `runs`, `answers` or `restores`, see `Repro` in `fuzz/`), which `RegressionJournalsTest` checks per file. A fuzz
  journal that a `FORMAT_VERSION` change makes unloadable is deleted; the core test of its fix stays. A change that
  only adds types leaves the journals' entries as they are: their `formatVersion` goes up with it, and
  `RegressionJournalsTest` shows they still load. A journal
  can't check a fix that changes what an earlier event queued (a decision offered without an answer, a stale queued
  event): the journal records the followup, so a restore queues it again whatever the fix. Such a fix has its core
  test only.
- Flyway migrations in `src/main/resources/db/migration`. The tests (`BackendTest`) run against a Testcontainers
  Postgres with the `dev` profile; `LiveGameTest` drives real web socket clients and restarts the server mid-game, and
  `SandboxLiveTest` plays, undoes, exports and imports a sandbox. `SeatsTest` covers the seats, votes and host tools, `SeatsLiveTest` the reconnect on a seat change and the read-only
  link. `LiveGameTest`, `SandboxLiveTest` and `GameSessionsTest` play `ScriptedTurns`
  beside an engine in memory, which asks for every answer, so they turn `frakkin.take-only-answers` off;
  `OnlyAnswersTest` plays with it on.
- CI and deploy (`.github/workflows/ci.yml`, its header lists the secrets): `build` runs `./gradlew build` and keeps
  the jar, `e2e` plays `npm run e2e` against that jar with the `dev` profile and a Postgres service on 5433, and on
  master `deploy` uploads the same jar with the `Dockerfile` (which packs a built jar, it doesn't build one) to the
  VPS over ssh and runs `deploy/deploy.sh` there: it builds the image, runs it on the host's network (the VPS's
  Postgres is at localhost) with `~/frakkin-toasters/app.env.next`, waits for `/api/login/providers`, and keeps the
  settings as `app.env` once the app answers; otherwise it starts the previous image with the previous `app.env` and
  fails. The wrapper (`gradlew`, `gradle/wrapper/`) is in git for the runners.
