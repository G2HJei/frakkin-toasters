# frakkin-toasters

Free version of Battlestar Galactica the board game.

## Core Architecture

`GameEngine` runs a game. A player answers what the game waits for with `submit` (a concrete event) or `choose` (an
option of a `OneOf`), and the engine runs the game forward until a player has to act again. Every event has a
processor that reads the game through `GameView` and changes it only through game ops, and every change is a fact in
the journal, so a game can be restored from its journal alone.
```text
                             frontend / tests
                                    |
         submit(player, event)      |      status(player)  Waiting | Finished
         choose(player, option)     |      view()          GameView
                                    v
+-----------------------------------+--------------------------------------+
|                                GameEngine                                |
|                                                                          |
|  +----------------------------+  accepted  +-------------------------+   |
|  | Input check                +----------->|        Run loop         |   |
|  | player, type, constraints  |            |                         |   |
|  +----------------------------+            | 1 take next event       |   |
|                                            | 2 validate              |   |
|  +----------------------------+    next    | 3 open scope            |   |
|  | Agenda                     +----------->| 4 process               |   |
|  | stack of pending followups |<-----------+ 5 win checks            |   |
|  +----------------------------+  followup  | 6 close scope           |   |
|                                            | 7 push followup         |   |
|  +----------------------------+   create   |                         |   |
|  | Processor registry         |<-----------+ stops at a decision     |   |
|  +----------------------------+            | or when the game ends   |   |
|                                            +---+-----------------+---+   |
|  +----------------------------+  scopes,       |                 |       |
|  | Journal                    |  followups,    |                 |       |
|  | scopes, facts, followups,  |  choices       |                 |       |
|  | choices                    |<---------------+                 |       |
|  +-------------^--------------+                                  |       |
|                | append                                          |       |
|  +-------------+--------------+                                  |       |
|  | FactRecorder               |                                  |       |
|  | apply, then append         |                                  |       |
|  +----+-----------------^-----+                                  |       |
|       |                 |                                        |       |
+-------+-----------------+----------------------------------------+-------+
        |                 |                                        |
        | apply           | record(fact)                           | event
        |                 |                                        v
        |    +------------+-------------+  calls   +---------------+-------+
        |    | GameOps                  |<---------+ XxxEventProcessor     |
        |    | PlayerOps, ShipOps, ...  |          | prepare(): view only  |
        |    +--------------------------+          | run(ops): view + ops  |
        v                                          +---------------+-------+
+-------+----------------------------------+                       |
| Game                                     |  reads                |
| boards, decks, players, ships, titles    |<----------------------+
|                                          |  GameView
| +--------------------------------------+ |
| | Turn                                 | |
| | number, step, current, acting player | |
| +--------------------------------------+ |
+------------------------------------------+
```

## Backend

The `backend` module serves the engine to browsers (`backend.drawio`). Users log in through Google
or Discord, host and join games in the lobby, and play or observe them over a web socket. Each game runs one
`GameEngine`, its journal is stored in Postgres, and every viewer gets only what he may see: the `projection/` package
of `core` builds the game and the log per viewer.

```text
                                  user browser
            |                          |                              ^
            | REST                     | web socket                   | web socket
            | log in, me,              | status / submit / choose     | game / update / log / rejected
            | host, join, start        |                              |
            v                          v                              |
+-----------+-----------+   +----------+-----------+      +-----------+-----------+
| Lobby and users       |   | LiveConnection       |<-----+ ConnectionPool        |
| Google / Discord auth |   | user, viewer, game   |      | game -> connections   |
+-----------+-----------+   +----------+-----------+      +-----------^-----------+
            |                          | command                      | push in order
            |                          v                              |
            |          +---------------+------------------------------+-----------+
            |  start   | GameSessions                                             |
            +--------->| lock the game, get the session, check the token,         |
            |          | run the engine, store the entries and the summary,       |
            |          | build and queue each viewer's projections, unlock        |
            |          +-------+-------------------------------+------------------+
            |                  | cache-aside                   | JournalCodec (JSON)
            |                  v                               v
            |          +-------+-------------+   +-------------+------------------+
            |          | GameSession (cached)|   | Postgres                       |
            |          | GameEngine          |   | journal_entry, game_bundle,    |
            |          | start / restore     |   | participant, app_user,         |
            +--------->|                     |   | user_identity, sessions        |
                       +---------------------+   +--------------------------------+
```

## Frontend

The `frontend` module is a React app: the website (login, the lobby with its games, game rooms,
settings) and the table, where a game is played over the web socket. The table shows exactly what the server's
projection holds: the board as our own schematic, the players, the turn, the skill check, the decks and the log. A
pending input comes with its legal answers, which the input panel lists by what they do; many are answered on the
board too (a move by clicking a location, an action by the "Act" button on your location). The browser holds no game
rules: the engine judges every answer.

A **sandbox** is a game one user plays for every seat: he sees everything, the draw piles too, and can undo, restart
with the same seed, set up scenarios (stack a card, give a card, move a character, place a ship, change a resource,
...) and export the journal, or import one.

### Running it locally

Docker has to run, for Postgres and for the backend's tests (Testcontainers).

```bash
docker compose up -d                                              # Postgres on port 5433
./gradlew :backend:bootRun --args='--spring.profiles.active=dev'  # the app on http://localhost:8080
```

`./gradlew build` builds and tests everything, and packs the built frontend into the backend's jar, so
`java -jar backend/build/libs/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev` serves the whole app too. To work
on the frontend with hot reload, run `npm run dev` in `frontend/` and open http://localhost:5173 (it proxies the API
and the socket to 8080). Log in by name, start a sandbox with a seed, and play.

The `dev` profile has a login by name (`POST /api/dev/login`), so no client secrets are needed. Without it, set
`FRAKKIN_LOGIN_GOOGLE_CLIENTID` / `..._CLIENTSECRET` and `FRAKKIN_LOGIN_DISCORD_CLIENTID` / `..._CLIENTSECRET`. The
`Dockerfile` builds one image; its header lists the settings a deployment gives (the database, the logins and
`TOKEN_SECRET`).

- `npm test` in `frontend/` runs the unit tests, and `npm run e2e` the Playwright specs against the running app
  (`npx playwright install chromium` once).
- After a change to an event, a fact or a projection: `./gradlew :backend:exportSchema`, then `npm run gen`.

### A sandbox bug becomes a test

1. Export the sandbox's journal ("Export" on the table).
2. Drop the file into `backend/src/test/resources/journals/`. `RegressionJournalsTest` checks that every file there
   restores.
3. Write a test that loads it with `RegressionJournals.engine("the-file.json")`: the game as it stood, seeded, so the
   next input gives what the sandbox gave. Assert what should have happened.

## Turn cycle

The turn sequence is ordinary events on the agenda (rule 8.1). The engine knows nothing about steps.

```text
StartGameEvent --> setup followups
                         |
                         v
                  StartTurnEvent <--------------------------+
                         |                                  |
                         v                                  |
                   Receive Skills                           |
                         |                                  |
                         v                                  |
                      Movement                              |
                         |                                  |
                         v                                  |
                       Action                               |
                         |                                  |
                         v                                  |
                       Crisis                               |
                         |                                  |
                         v                                  |
              Activate Cylon Ships (icon)                   |
                         |                                  |
                         v                                  |
                Prepare for Jump (icon)                     |
                         |                                  |
                         v                                  |
                      Discard                               |
                         |                                  |
                         v                                  |
                    EndTurnEvent ------ next player --------+
                         |
                         | a resource is at 0
                         v
                     Cylons win
```

## Agenda

The followups waiting to run. It is a stack, because followups run depth first: if `all(A, B)` is queued and `A`
queues `C`, then `C` runs before `B`.

The engine reads its status from the top of the agenda: the game is finished, or it waits for a player action or a
choice.

## Journal scopes

Here is the scope logic, traced through one example. Event `A` queues `all(B, one(C, D))`, and the player then picks
`C`.

1. The journal is a flat list, written in this order:

```text
 #   entry                                       open scope   currentScope()
---  ------------------------------------------  ----------   --------------
 1   FactRecorded(0, GameStarted)                    -         0 = ROOT
 2   ScopeOpened(1, parent=0, A, actor=1)            1         1
 3   FactRecorded(1, f1)                             1         1
 4   ScopeClosed(1, all(B, one(C, D)))               -         0
 5   ScopeOpened(2, parent=1, B, actor=2)            2         2
 6   FactRecorded(2, f2)                             2         2
 7   ScopeClosed(2, NONE)                            -         0
 8   ChoiceMade(1, chooser=1, C)                     -         0     <- between events
 9   ScopeOpened(3, parent=1, C, actor=1)            3         3
10   FactRecorded(3, f3)                             3         3
11   ScopeClosed(3, NONE)                            -         0
```

`A` and `C` are plain events, so their actor is the acting player (1). `B` is a `PlayerEvent` of player 2, so it is
logged under player 2. The chooser is 1 because the options name no player, so it is the current player.

2. Over time, at most one scope is open:

```text
time ------------------------------------------------------------------------>

         +--- scope 1: A ---+     +--- scope 2: B ---+         +--- scope 3: C ---+
ROOT ----+  f1              +-----+  f2              +----+----+  f3              +---- ROOT
         +------------------+     +------------------+    |    +------------------+
          closes with                                     |
          all(B, one(C, D))                         ChoiceMade(1, 1, C)
          no scope open here
```

3. The tree that `parentId` rebuilds:

```text
ROOT (0)
 |-- f0  GameStarted
 `-- scope 1  A                         queued all(B, one(C, D))
      |-- f1
      |-- scope 2  B                    parent = 1
      |    `-- f2
      |-- choice: player 1 picks C      ChoiceMade under 1, the scope that queued the OneOf
      `-- scope 3  C                    parent = 1
           `-- f3
```
