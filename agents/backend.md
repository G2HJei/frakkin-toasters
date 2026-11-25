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
  session, which Spring Session keeps in Postgres so a login survives a restart, `GET`/`PUT /api/me`. CSRF through the
  `XSRF-TOKEN` cookie and the `X-XSRF-TOKEN` header.
- `lobby/` - `/api/games`: host, list (open lobbies, mine, running ones to watch), join as a player (next free seat, 7
  at most) or an observer, leave, and start by the host with 3 to 7 players (the seats close up to 1..n).
- `live/` - `/ws/games/{id}`: the handshake needs a participant and an allowed `Origin`, and gives the connection its
  `Viewer`. `ConnectionPool` is the `ProjectionSink`; each `LiveConnection` sends from its own queue. Client messages:
  `status`, `history`, `submit` (token, event: an answer to the pending decision, or an `AnytimeEvent`, which
  `GameSessions` runs ahead of it, with the player of the connection, the one it names in a sandbox), `choose` (token,
  option index). Server messages: `game` (token,
  projection), `update`, `log`, `rejected`. A new connection gets `game`, then `log`. A game that can't be opened
  (e.g. saved by another `FORMAT_VERSION`) gets `rejected` (`UNAVAILABLE`, with the reason) and then the close 1008
  (`LiveConnection.closeAfterSending`), after which `GameSocket` doesn't reconnect; the table shows the reason, and the
  room's `unavailableReason` before any socket.
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
  `SandboxLiveTest` plays, undoes, exports and imports a sandbox. Those two and `GameSessionsTest` play `ScriptedTurns`
  beside an engine in memory, which asks for every answer, so they turn `frakkin.take-only-answers` off;
  `OnlyAnswersTest` plays with it on.
