# GameView + game ops: review

## Question

> ok, finally i decided to have a `GameView` be passed to the processors so I can keep the utility methods mutating
> stuff. I will also introduce new game op classes (better name?) that will be used to mutate the state and log the
> event
> appropriately when needed. This way I can test the game ops separately for the logging. They will have to be more
> granular, for example draw a card and give to player hand will be in one game op.
>
> Describers will still be needed to describe the effects logged by the game ops.
>
> ```text
> view path: Processor -> GameView
> mutate path: Processor -> game op -> Game components
> ```
>
> Review this variant. Also draw an ASCII diagram for it.

## Answer

This variant works well. Processor code stays close to what it is today, and processors can read their own changes:
`FTLControlEventProcessor` can check population right after reducing it. All the logging sits in a few classes you can
test on their own. It also fixes a weakness of the effects approach: a game op has the whole game at hand, so
`destroyShip(2)` can record the raider's type and location before removing it, without the processor passing them in.

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

### What to watch

**1. The view needs to be airtight.** Several components hand out writable internals today, and some can be changed
without even a cast:

- `SpaceLocationsBoard.shipsInSpace()` returns the board's own map, and `place`/`remove` are default methods that write
  through it.
- Lombok `@Getter` exposes raw collections: the piles in `SkillCheck`, `Player.revealedCivilianShips()` and
  `Game.removedComponents()`. Declaring them as `List` in an interface doesn't help, because `List` has `add`.
- Getters return writable objects: every `Deck` (player hands and `DecksHolder`), `Player`, ships with setters
  (`HumanFighter.pilot(Character)`, `Basestar.damage(...)`), and `Game.die()`.

Fixes:

- Have components return copies or view types, and give each kind of write its own method.
- Return snapshots (`List.copyOf`, stream `.toList()`), not live read-only wrappers. A processor can then loop over
  ships while a game op moves them. `AdvancePursuitTrackEventProcessor` already copies the map with `new HashMap<>(...)`
  for that reason.
- As a catch-all, tag the write methods (e.g. `@Mutates`) and add an ArchUnit test that `..event..` never calls them.
  That also catches a processor casting the view back to the real class.

**2. Give processors the game ops only for `process()`.** Pass them as a parameter, or set the field in the base class
just before calling `process()`. Then `init()` and `isValid()` can't change state.

**3. The methods you're keeping have hidden side effects.** A game op logs what it believes it did, so a component
method that does more than its name says leaves gaps in the log:

- `Deck.draw()` reshuffles the discard pile when the deck runs out. The log would say "P1 drew X" but never "Politics
  deck reshuffled".
- `Game.damage(location)` also sends everyone in that location to Sickbay.
- `Game.destroy(basestar)` discards its damage cards and returns it to the supply, and `CylonShips.returned()` clears
  the damage.

Make component methods do one thing, or report what else they did. Then the game op runs each step and logs the ones
that matter.

**4. Rules that keep game ops trustworthy:**

- One call is one fact, at the level a player would say out loud ("P1 drew 2 Politics cards"). Your draw-to-hand example
  is exactly that level.
- Log after the change succeeds, using the same values that made the change.
- Build bigger operations from smaller game ops instead of going around them. For example, `damageLocation` calls
  `moveCharacter` for each character there, so the Sickbay moves are logged as their own facts under it.
- Always log, instead of "when needed". Describers decide what to show and can return nothing for bookkeeping facts. A
  complete log can later drive replay, reconnects or client sync, and it keeps game op tests uniform.
- Log the full data, such as the actual cards drawn. Hiding things from other players is the describers' job.
- Game ops do mechanics, not decisions. Picking targets, "destroyed after 3 hits" and raider movement stay in
  processors. Consequences that always come with an operation belong in the game op, like Sickbay on location damage or
  a reshuffle when a deck runs out.
- Return immutable values or view types (drawn cards, ship ids), never writable components.
- Game ops shouldn't take an event parameter for logging. The engine opens the journal scope, and game ops just append
  to it.

**5. Only game ops should write, the engine included.** Component write methods stay public, so anything holding a
`Game` could change it without logging. That includes the future engine advancing `step` or `currentPlayerNumber`, and
setup
code. A second ArchUnit rule, allowing only `..operation..` and tests to call `@Mutates` methods, keeps the log
complete.

**6. Tests:**

- Game op tests check the state change and the logged fact.
- Processor tests use real game ops and assert the logged facts and the followup. Don't mock the game ops: a mock
  changes nothing, so a processor that reads its own changes would see stale state.
- Test setup can keep using the concrete classes directly, without logging. Clear the journal before `execute`.

**7. If you want replay later:** have each game op build its fact and apply it through a single `apply(fact)`. Replay
then reuses the same code instead of needing a second implementation. You don't need this now.

### Naming

- **Game operations** (game ops) is accurate even for calls that don't change state, like `rollDie()` or "P1 looked at
  the top crisis card". For precedent: Forge, the open-source Magic: The Gathering engine, is built along similar lines.
  Its rules code calls a `GameAction` class to move cards, and a log formatter turns the resulting events into log
  lines. I wouldn't use "Action" here, because it clashes with BSG's Action step and your `ActionEvent`.
- Group the classes by area (cards, ships, characters, resources and tracks, titles, skill check) behind one entry
  object, so processors only hold one thing.
