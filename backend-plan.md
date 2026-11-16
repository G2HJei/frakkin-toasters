# Backend plan

Implements `backend.drawio`: users log in, host and join games, and play or observe them over a web socket. A new
`backend` module runs one `GameEngine` per game, stores its journal in Postgres and pushes to every viewer what that
viewer may see. It closes what roadmap step 3 left open (JSON serialization, describers and per-viewer hiding) and is
what the frontend of step 5 talks to.

Every phase ends with the tests green. The plan is done when three browsers-as-test-clients can log in, host, join and
play a seeded game to its end over the web socket, the server can be restarted at any pause without anyone noticing, and
no frame a client receives holds something that client may not know.

## Target design

```text
                                  user browser
            |                          |                              ^
            | REST                     | web socket                   | web socket
            | log in, me,              | status / submit / choose     | game / update / rejected
            | host, join, start        |                              |
            v                          v                              |
+-----------+-----------+   +----------+-----------+      +-----------+-----------+
| Lobby and users       |   | LiveConnection       |<-----+ ConnectionPool        |
| Google / Discord auth |   | user, viewer, game   |      | game -> connections   |
+-----------+-----------+   +----------+-----------+      +-----------^-----------+
            |                          | command                      | 9 push in order
            |                          v                              |
            |          +---------------+------------------------------+-----------+
            |  start   | GameSessions                                             |
            +--------->|                                                          |
            |          | 1 lock the game             6 on a failed 5: evict       |
            |          | 2 get the session           7 build and queue projections|
            |          | 3 check the sequence        8 unlock                     |
            |          | 4 engine.submit / choose                                 |
            |          | 5 append the new entries and update game_bundle (1 trx)  |
            |          +-------+-------------------------------+------------------+
            |                  | cache-aside                   | JournalCodec (JSON)
            |                  v                               v
            |          +-------+-------------+   +-------------+------------------+
            |          | GameSession (cached)|   | Postgres                       |
            |          | GameEngine          |   | journal_entry                  |
            |          | start / restore     |   | game_bundle, participant       |
            |          +---------------------+   | app_user, user_identity        |
            +----------------------------------->|                                |
                                                 +--------------------------------+
```

The engine already has everything `GameSessions` needs: `submit` and `choose` are all-or-nothing, `restore` rebuilds a
game from its journal alone, and `status(player)` hides a secret input. The backend adds no game rules. Its own work is
the three things the engine left to its caller: one call at a time per game, a journal that survives the process, and
a view of the game for each viewer.

### One command

A command is a `submit` or a `choose` from a player's connection. `status` is a read: it takes the lock and the
session, and answers the sender with his projection.

1. **Lock.** One lock per `gameBundleId`, so the calls of a game run one at a time. The engine leaves that to its
   caller.
2. **Session.** A cache hit, or load the journal rows, decode them and `GameEngine.restore`.
3. **Sequence.** The sequence of a game is the number of its journal entries. A command names the sequence it answers.
   If the game has moved on, the command is stale: it is rejected and the sender gets the current projection.
4. **Engine.** An `InvalidActionException` goes back to the sender and nothing changed. A
   `FrakCallTheAdmiralException` means the engine rolled back: log it with the game and the sequence, tell the sender,
   nothing to store.
5. **Store.** The entries after the old sequence and the `game_bundle` summary, in one transaction.
6. **Evict.** If 5 failed, the cached engine is ahead of the stored journal, so the session is dropped. The next
   command restores it from the rows.
7. **Project.** For every connection of the game: the new entries and the game as its viewer may see them, put on the
   connection's queue. This happens under the lock, so the queues get the commands in the order they ran.
8. **Unlock.**
9. **Push.** Each connection sends its queue in order. A slow browser holds up only itself, never the game.

### Tables

As drawn, with the keys spelled out. Flyway creates them.

- `app_user`: `id` uuid PK, `name`, `settings` jsonb.
- `user_identity`: `provider`, `subject`, `user_id` FK. PK (`provider`, `subject`).
- `game_bundle`: `id` uuid PK, `name`, `status`, `turn`, `host_user_id` FK, `winner_team`,
  `waiting_for_player_number`, `created_at`, `updated_at`, `format_version`. `status` is `LOBBY`, `RUNNING` or
  `FINISHED`.
- `participant`: `game_bundle_id` FK, `user_id` FK, `player_number`. PK (`game_bundle_id`, `user_id`), UNIQUE
  (`game_bundle_id`, `player_number`). An observer has no `player_number`.
- `journal_entry`: `game_bundle_id` FK, `sequence`, `entry` jsonb. PK (`game_bundle_id`, `sequence`). `sequence` is
  the position of the entry in the journal, from 1.

## Decisions

1. **Spring Boot on Java 21, in a new `backend` module.** Web MVC for the lobby, its plain web socket support for the
   game, Spring Security for the OAuth logins, Flyway for the schema, Testcontainers for the tests. SQL goes through
   `JdbcClient`, not JPA: five tables, one of them append-only JSON, and no object graph to map. No STOMP either: the
   protocol is five message types.
2. **`core` stays free of libraries.** The JSON codec needs Jackson, so it lives in `backend`. The projections are game
   rules (who may know what), so they live in `core`, in a new `projection/` package, and are tested without Spring.
3. **The journal is the only game state that is stored.** `turn`, `status`, `winner_team` and
   `waiting_for_player_number` are a summary for the lobby, written in the transaction that appends the entries. Nothing
   reads them back into a game.
4. **Stored before shown.** Projections are built after the transaction commits, so no client ever sees a state the
   server could lose.
5. **One server instance.** The lock and the cache are in the JVM. The primary key of `journal_entry` still refuses a
   second writer, which then evicts and reloads. Several instances need a lock in Postgres or sticky routing, later.
6. **Clients get projections, never journal entries or followups.** A followup can name what is hidden: the decisions
   that `OpenInterruptWindowEvent` queues list who holds a card. The pending input goes to the player who answers it.
   The others learn who is asked, and not even that while it is secret.
7. **Projections are data, not text.** A type and its fields, e.g. `CardsGained(player 2, SKILL, 2 cards, [POLITICS,
   LEADERSHIP])`. The frontend owns the wording, so "describers" become projectors.
8. **What a player learns is a fact.** The projector never guesses from the type of an event who saw a card. Where a
   player looks at cards that don't go to his hand, the ops record it, as `LoyaltyCardSeen` and `CivilianShipRevealed`
   already do. So the update a viewer gets is a function of the journal and the viewer alone, and can be served again
   after a reconnect.
9. **The server fills in what a client must not forge.** The player number comes from the connection, a choice is the
   index of an option, and the engine already overwrites the constraints.
10. **The client names a state, not a sequence.** The token it echoes is the sequence hashed with a server secret.
    A bare sequence would let a client count the entries it wasn't shown, e.g. how many players passed in an interrupt
    window.
11. **One journal format at a time.** `format_version` is the version of the codec that wrote a game. A game of
    another version can't be opened and the lobby says so. An upcaster comes when a game worth keeping needs one. A
    test fails when the format changes without a new version.
12. **Logins only through Google and Discord.** No passwords. A server-side HTTP session, and the web socket handshake
    uses its cookie. A `dev` login behind a Spring profile, for local play and the tests.

## Phase 1 - The `backend` module

A running application with a database and nothing to play yet.

- `settings.gradle` includes `backend`. The root `build.gradle` sets the Java 21 toolchain for both modules. `backend`
  depends on `core` and on the Spring Boot starters of decision 1, with the JUnit version `core` uses.
- Package `xyz.zlatanov.frakkintoasters.backend`. `ProcessorRegistry` scans the root package, so nothing in `backend`
  may be named `XxxEventProcessor`.
- `compose.yaml` with Postgres for local runs. Flyway `V1` creates the five tables. `game_bundle.host_user_id`
  references `app_user`, which the diagram doesn't draw as an arrow.
- **Check `ProcessorRegistry` in the packaged jar.** It walks the classpath as directories or as a plain jar. Inside
  the Spring Boot jar `core` is a nested jar with another URL scheme. A test starts the built jar and runs one event.
  If the scan fails there, fix it in `core`. Also turn its `assert constructor != null` into a
  `FrakCallTheAdmiralException`: asserts are off in production, so a missing processor is a `NullPointerException`.
- Tests: the context starts against a Testcontainers Postgres and the schema matches the table above.

## Phase 2 - The journal as JSON

`JournalCodec` in `backend`: a `JournalEntry` to JSON and back. What `restore` gets back has to equal what was
written, because `AgendaReplayer` compares events with `equals`.

- **Type names.** An event is named by its simple class name: the 209 event records have no two alike, and a test
  keeps it so. A fact is named with its group, e.g. `DeckFact.CardsDrawn`, because `SkillCheckFact` has a `CardsDrawn`
  too. The names come from a closed registry, so JSON can't make the codec load a class.
- **The event catalog.** `core` gets a list of every event type under the root package, next to `ProcessorRegistry`
  and found the same way. It includes the events without a processor, which followups still hold
  (`PlayerDecisionEvent`, `PlayQuorumCardEvent`, ...), and the interfaces, because `PlayerDecisionEvent.action` can be
  `ActionEvent.class`.
- **The awkward shapes**, each with its own (de)serializer or a small change in `core`:
    - `PlayerDecisionEvent.action` is a `Class`, written as its type name.
    - The card lists of `DeckFact` and `PlayerFact` are `List<?>`. `DeckId` and `PlayerPile` get a `cardType()`, an
      exhaustive switch, so the codec knows what to read. The cards are enums, `SkillCard` or `CivilianShip`.
    - `OneOf` keeps the order of its options (a `LinkedHashSet`), because a choice is answered by index.
    - The sealed `InterruptWindow` and `Followup`, and the `Map` and `Set` fields of a few events.
- `JournalCodec.FORMAT_VERSION = 1`.
- **Test fixtures.** `core` applies `java-test-fixtures` and moves there what `backend` tests need: `GameSnapshot`,
  the scripted scenario of `GameReplayerTest` that records every kind of fact, the test-only events with their
  processors, and a way to start an engine with a first event of the test's choice.
- Tests:
    - Every event record and every fact record goes to JSON and back and is equal. The samples are built by reflection
      from the record components, and the builder fails on a component type it doesn't know, so a new shape can't slip
      through.
    - The journal of the scripted scenario, and of a seeded game played for some turns, goes through JSON and
      `restore` gives the same `GameSnapshot` and the same statuses.
    - The format lock: a fingerprint of every type name and its components is checked in. When it changes, the test
      says to raise `FORMAT_VERSION`.

## Phase 3 - `GameProjection`

In `core`, package `projection/`. `GameProjection.of(view, status, viewer)` is the game as one viewer may see it, as
plain records that `backend` turns into JSON. A `Viewer` is a player number or an observer, who sees what is public.

What lies open on the table is public: the boards, ships, resources, tracks, titles, characters and their locations,
the turn and its step, the die roll, the Crisis Cards of the turn and those kept in play, the destinations, and the
skill check's type, target, divided piles, strength and result. The rest:

| What                                            | Who sees it                                             |
|-------------------------------------------------|---------------------------------------------------------|
| A player's Skill Cards                          | He: the cards. Others: how many of which color          |
| Loyalty, Mutiny, Super Crisis, Motive Cards     | The holder. Others: the count and the revealed cards    |
| The Mutineer card                               | Everyone, once it is revealed (`RevealMutineerEvent`)   |
| The President's Quorum hand                     | The President. Others: the count                        |
| `seenLoyaltyCards`                              | The player who looked                                   |
| Decks                                           | Counts. A faceup discard pile: its cards                |
| Cards played before a skill check or into it    | The count per player, until they are revealed           |
| Cards in an open interrupt window               | The player who played them                              |
| Civilian ships in space                         | Everyone a ship. Its cargo: the players who looked      |
| The pending input                               | Decision 6                                              |
| Anything, once the game has ended               | Everyone                                                |

- **The pending input.** For the player who answers: the decision with its action name and constraints, or the choice
  with its options in order. For the others `waiting for player n`, or a plain `waiting` while it is secret. A finished
  game gives the winning team and the winners.
- **An open interrupt window must not show in the hands.** A card played into it left the hand, so the count the others
  see adds the window's cards back until the window closes.
- **Civilian ships.** A ship is named by its id, and `AllCardsProvider` gives the same id to the same cargo in every
  game, so the id gives the ship away. This is its `todo randomize civ ship ids`. The shuffle of the civilian ship deck
  deals the ids to the cargo anew, at the start and every time ships go back into the deck, and `DeckShuffled` holds
  the result. A player's `revealedCivilianShipIds` forget a ship that goes back. So an id says nothing until a player
  looks, and nothing again after a jump, and neither the projections nor the submitted events need another name for a
  ship.
- `ArchitectureTest`: `..projection..` depends on the views and the journal, never on `..operation..`, and nothing in
  `core` depends on it.
- Tests: one per row of the table, for the holder, another player and an observer. Then at every pause of a seeded
  game: the projection of a viewer doesn't change when only another player's hidden cards are swapped for others of
  the same colors.

## Phase 4 - `LiveUpdateProjection`

Also in `projection/`. `LiveUpdateProjection.of(entries, viewer)` is what happened, as one viewer may see it: the
scopes in order, each with its actor, the name of its event and its facts.

- **Facts.** An exhaustive switch over `Fact`, so a new fact doesn't compile until it has a rule. Most are public
  and pass as they are. A fact with cards follows the table of phase 3: `CardsGained(SKILL)` gives the others the
  count and the colors, `DeckShuffled` never its order, `CardsDrawn` only a count, because the fact that says where
  the cards went decides who sees them.
- **What a player learned (decision 8).** Go through every place where a player looks at cards he doesn't take, e.g.
  the Admiral's two Destination Cards and "Gas Cloud". Each records a fact that names the player and the cards, and
  applies nothing else. This settles the `the describers have to hide it` comment in
  `DrawDestinationCardsEventProcessor`.
- **Events.** Only the name and the actor are shown by default, because the facts tell what happened. An event whose
  fields say something the facts don't (the section of a Crisis Card, a declined option) is marked public. A test
  lists every event component that holds a card and fails until each is marked public or private, so
  `PlayCardsIntoSkillCheckEvent.skillCards` or `ChooseDestinationEvent.other` can't leak by default.
- **Secret scopes.** The scope of a `SecretEvent` shows only to its actor. `InterruptCardResolved` and
  `InterruptCardReturned` are public and tell the others what was played once the window closes.
- **`ChoiceMade`** shows who chose. The option shows only when its events are public.
- **Followups** in `ScopeClosed` are never shown.
- Tests: one per fact, like `FactApplierTest`. `InterruptWindowTest`'s script: the others get an empty update while
  the window is open, and the played card when it closes. A skill check: nobody sees who played which card.

## Phase 5 - `GameSessions`

In `backend`, with no web layer yet. The flow of "One command", against real Postgres.

- `JournalStore`: append a batch of entries from a sequence on, and load a game's entries in order. `GameBundles`:
  create, update the summary, find.
- `GameSession`: the `gameBundleId` and the engine. It asks the engine for `journal()` and `view()` on every use,
  because a rollback replaces both.
- `GameSessions.start(gameBundleId, playerCount)`: `GameEngine.start` with a fresh `Randomness`, then the entries up to
  the first pause and `status = RUNNING` in one transaction. The `Randomness` comes from a bean, so a test can seed it.
- `GameSessions.submit`, `choose` and `status`, each with the viewer and the token. A submitted event is decoded only if
  its type fits the pending decision, and its `playerNumber` is set from the viewer.
- The lock is a `ReentrantLock` per game. The cache is Caffeine, dropping a session that nobody used for a while.
- The summary: `turn` from `TurnView.number()`, `waiting_for_player_number` from the pending input, `FINISHED` and
  `winner_team` from the outcome. The lobby tells a user only whether a game waits for him, so the column can hold the
  player of a secret input.
- Projections go to a `ProjectionSink` interface, which phase 8 implements with the connections. A connection gets
  nothing when its viewer's update is empty and his game is unchanged, so a secret window stays silent for the others.
- Tests:
    - A scripted game with the session evicted after every command: each command restores from the rows, and the
      `GameSnapshot` matches a game played in memory.
    - Two threads answer the same input: one is accepted, the other is stale.
    - A failed append (a row put in the way) evicts the session, and the next command sees the stored game.
    - The test-only `FailingEvent`: the engine rolls back, no row is written, the session stays.
    - A game of another `format_version` isn't opened.

## Phase 6 - Users and logins

- Spring Security OAuth login with two providers. Google is built in. Discord is a custom registration, because
  Spring has no preset for it.
- A first login creates the `app_user` and its `user_identity`. A later one finds the user by `(provider, subject)`.
  Nothing else of the provider's profile is stored.
- `GET /api/me`, and `PUT /api/me` for the name and the settings ("Register / update settings").
- CSRF protection stays on for the REST calls, and the web socket handshake checks the `Origin`.
- The `dev` profile adds a login by name, so the app runs without client secrets. It can't be on in production: the
  application refuses to start with both `dev` and a real provider configured.
- Tests: a login creates a user once, a second login reuses it, and the API refuses a caller who isn't logged in.

## Phase 7 - Lobby

REST over `game_bundle` and `participant`.

- **Host**: `POST /api/games` creates a `LOBBY` game with the codec's `format_version`, and seats the host.
- **List**: the open lobbies, and my games with whether one waits for me.
- **Join** as a player (the next free seat, 7 at most) or as an observer. A user is in a game once, as one or the
  other. After the start only observers can join. **Leave** while it is a lobby.
- **Start**, by the host, with 3 to 7 players, which is what `CreateLoyaltyDeckEventProcessor` deals for. The seats
  are closed up to 1..n, because the engine numbers the players that way and the numbers are the clockwise order.
  Then `GameSessions.start`. Characters are chosen in the game, through `SelectCharacterEvent`.
- Tests: the rules above, and two users racing for the last seat.

## Phase 8 - Live connection

The web socket of the diagram: `/ws/games/{gameBundleId}`.

- The handshake needs a logged-in user who is a `participant` of the game. That gives the `LiveConnection` its user,
  its game and its viewer: player n or observer.
- `ConnectionPool` maps a game to its connections and implements `ProjectionSink`. Each connection has its own
  outgoing queue with one writer, a send time limit and a buffer limit. One that falls behind is closed and can
  reconnect.
- Messages, as JSON:

  | From   | Type       | Content                                                         |
  |--------|------------|-----------------------------------------------------------------|
  | Client | `status`   | Nothing                                                         |
  | Client | `submit`   | The token and an event                                          |
  | Client | `choose`   | The token and the index of an option                            |
  | Server | `update`   | A `LiveUpdateProjection`                                        |
  | Server | `game`     | The token and a `GameProjection`                                |
  | Server | `rejected` | Why: stale, invalid (the engine's message), or a server failure |

- A new connection gets a `game` message. After a command, the connections that have something new get the `update`
  first and the `game` after it. A `rejected` goes only to the sender.
- A user can have several connections, e.g. two tabs. A reconnect is just a new connection.
- Tests, with real web socket clients against the running application:
    - Three players and an observer play the scripted turns. Everyone gets the messages in the order of the commands.
    - A submit from the wrong player or with an old token is rejected and nobody else hears of it.
    - During an interrupt window the others get no frame.
    - No frame of a client holds a card that only another player knows.
    - The application restarts in the middle of a game, the clients reconnect and the game goes on.

## Phase 9 - Docs and the way to run it

- `CLAUDE.md`: the `backend` module, its packages and build commands, `projection/` in `core`, the test fixtures, and
  roadmap step 3 closed. Each earlier phase already fixes the lines it makes stale.
- `README.md`: the backend next to the engine diagram, `backend.drawio` under "Design Documents", and how to run it
  locally with `compose.yaml` and the `dev` login.
- A `Dockerfile` for the application, and the settings a deployment has to give: the database, the two client ids and
  secrets, the token secret.

## Open questions

1. **Spring Boot** (decision 1). The phases stay as they are with another framework, only phases 1, 6 and 8 are wired
   differently.
2. **"Check sequence".** I read it as the client's check: a command names the state it answers. The other reading, the
   cached session against the stored journal, isn't needed on one instance, and the primary key covers it.
3. **The colors of facedown Skill Cards.** The backs of the real cards show the color, so the plan shows the others the
   colors of a hand and of the cards a player puts into a skill check. Hiding them would be kinder to the Cylons than
   the table is.
4. **Projections in `core`** (decision 2), not in `backend`.

## Not in this plan

- The frontend, roadmap step 5.
- The legal answers of a decision. The engine rejects an illegal one with a message, but nothing lists the legal moves,
  locations or cards. The frontend has to know the rules or try. A list in `core` would be its own plan.
- The events that are still open in the engine: Executive Order and the other `Action:` and `Movement:` skill cards,
  Cylon reveal, Quorum and Super Crisis cards.
- Chat, notifications when a game waits for you, bots, an admin view of a journal.
- Several server instances, and upcasters for an old `format_version`.
