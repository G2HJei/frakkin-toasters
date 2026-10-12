# frakkin-toasters

Free version of Battlestar Galactica the board game.

## Core Architecture

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
