# Game view

```text
+-----------------------------------------------------------------------+
|                                Engine                                 |
|  1 open journal scope (event, actor, parent)   2 run the processor    |
|  3 end-of-game checks   4 close scope   5 schedule returned Followup  |
+---+-----------------------------------------------^---------------+---+
    | event                                         | Followup      |
    v                                               |               | open/close
+---+-----------------------------------------------+---+           | scope
|                   XxxEventProcessor                   |           |
|   init(), isValid()   ->  view only                   |           |
|   process(gameOps)    ->  view + game ops             |           |
+---+---------------------------------------+-----------+           |
    | view path                             | mutate path           |
    v                                       v                       |
+---+-------------------+     +-------------+------------+          |
|       GameView        |     |      GameOperations      |          |
|  read-only types,     |     |  drawToHand(...)         |          |
|  snapshot collections,|     |  moveCharacter(...)      |          |
|  no Die               |     |  destroyShip(...)        |          |
+---+-------------------+     |  rollDie()  (log only)   |          |
    |                         +------+-------------+-----+          |
    | reads                   mutates|             | logs facts     |
    v                                v             v                v
+---+--------------------------------+---+   +-----+----------------+---+
|        Game components (state)         |   |          Journal         |
|  Game, boards, decks, players, ships,  |   |  scope: event, actor,    |
|  CylonShips, Die                       |   |         parent id        |
+----------------------------------------+   |  facts: [...]            |
                                             +------------+-------------+
                                                          |
                                                          v
                                             +------------+-------------+
                                             |        Describers        |
                                             |  per fact type,          |
                                             |  per viewer              |
                                             +------------+-------------+
                                                          |
                                                          v
                                                    frontend text
```

# Game engine

The game engine runs the game forward until a player has to act or the game has ended. The engine knows no game rules:
it runs the events on the agenda, and the processors decide.

## Target design

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

The engine has two public moves. `submit` answers a pending decision with a concrete event, and `choose` picks an option
of a pending `OneOf`. Both run the game forward until the next point where a player has to act, so after every call the
engine is either `Waiting(PendingInput)` or `Finished(winner)`. There is no "running" state to observe. `status` takes
the viewing player, because the input of an interrupt window is secret (decision 8).

`GameEngine.start(playerCount, randomness)` begins a game with `StartGameEvent` on the agenda, and
`GameEngine.restore(journal)` rebuilds one. `PendingInput` isn't stored. It is read from the top of the agenda, so
restoring the agenda restores it too.

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

# Journal scopes logic

Here is the scope logic, traced through one example. Event A queues all (B, one (C, D)), and the player then picks C.

1. The journal is a flat list, written in this order

```
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

A and C are plain events, so their actor is the acting player (1). B is a PlayerEvent of player 2, so it's logged under

The chooser is 1 because the options name no player (decision 7).

2. Over time, at most one scope is open

```
time ------------------------------------------------------------------------>

         +--- scope 1: A ---+     +--- scope 2: B ---+         +--- scope 3: C ---+
ROOT ----+  f1              +-----+  f2              +----+----+  f3              +---- ROOT
         +------------------+     +------------------+    |    +------------------+
          closes with                                     |
          all(B, one(C, D))                         ChoiceMade(1, 1, C)
          no scope open here
```

3. The tree that parentId rebuilds

```
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

# Agenda

The followups waiting to run. It is a stack, because followups run depth first: if all (A, B) is queued and A queues C,
then C runs before B.

The engine determines the EngineStatus from it to determine if the game is finished or is waiting for something to
happen - either a player action or a choice.

