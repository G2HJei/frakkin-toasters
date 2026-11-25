# Journal and replay

The journal records exactly what the game ops did, so a game can be rebuilt from the journal alone. It is fact-sourced: replay applies recorded facts, and never re-runs processors or needs randomness.

- `Fact` (`engine/journal/fact/`) is a sealed interface, grouped by area: `BoardFact`, `DeckFact`, `GameFact`,
  `InterruptFact`, `PlayerFact`, `ShipFact`, `SkillCheckFact`, `TitleFact`. The records are nested in the area interfaces, because a
  sealed hierarchy can't span packages without a `module-info`. A new fact has to be handled in `FactApplier`'s
  exhaustive `switch`.
- `Journal` is an append-only flat list of `ScopeOpened(scopeId, parentId, event, actor)` / `FactRecorded` /
  `ScopeClosed(scopeId, followup)` / `ChoiceMade(scopeId, chooser, option)`, and the engine's `EventInjected` and
  `OnlyAnswerTaken` (see [Engine](engine.md#engine)). It isn't part of `Game`: `GameEngine`
  owns it, and outside the engine whoever builds the ops does (`TestHarness`, `GameReplayerTest`). Ops never see it,
  they get a `FactRecorder(game, journal)`.
- At most one scope is open at a time. A followup runs after its parent's scope has closed, so
  `openScope(event, actor, parentId)` takes the parent explicitly, and `closeScope(followup)` records what the event
  queued. `recordChoice` logs the answer to a `OneOf` between events, under the scope that queued it. Facts recorded
  while no scope is open go under `Journal.ROOT_SCOPE`. `GameEngine` opens a scope per event after `prepare`, and so
  does `EventTestHarness.execute`.
- The scope's `actor` is the player who submitted the event. For a queued event, `ScopeActor.ofQueued` (`engine/`)
  gives the event's player for a `PlayerEvent`, otherwise the acting player.
- `FactApplier` is the only code that changes components. `FactRecorder.record(fact)`, which `AbstractOps.record` calls,
  applies a fact and then appends it, so live play and replay share one code path. `ShipZones` (same package) moves
  ships between zones by id.
- `GameReplayer.replay(journal)` builds the game from the first fact, `GameStarted`, and applies the rest.

Rules for facts and ops:

1. **A fact applies exactly what it says and nothing more.** No cascades: `LocationDamaged` marks the damage, and the
   Sickbay moves are separate `CharacterMoved` facts logged after it.
2. **Every random or looked-up result goes into the fact**: drawn cards, the order after a shuffle, die values, ship
   ids.
   Randomness comes from `Game.randomness()` (`Randomness`, seedable), never from `Collections.shuffle` or
   `new Random()`.
3. **Ops never write to components.** They compute a fact from the current state and call `record(fact)`. Drawing is
   `draw(DeckId)` (peek, record `CardsDrawn`, log the reshuffle when the draw empties a reshuffling deck), shuffling is
   `shuffle(DeckId)`. A drawn card is in no pile until another fact says where it went.
4. **Fact fields are complete and immutable.** Cards are listed in full. Hiding them is the projections' job (see
   [Projections](projections.md)).
5. **`@Mutates`** marks every component method that changes state. `ArchitectureTest` (ArchUnit) allows calls only from
   `..journal..` and `..state..`, and checks that `..event..` and `..operation..` never call them. Tests are not
   analyzed. Mutable collections that getters expose (e.g. `CylonShips.raiders()`) can't be marked, don't change
   state through them. Two more rules keep the layers apart: `..state..` depends on none of `..operation..`,
   `..event..` and `..engine..`, and `..event..` doesn't depend on `..engine..`.
6. **State getters return copies.** Lists are `List.copyOf`, so a list a caller holds stays as it was when a later fact
   changes the state, e.g. a loop that discards the cards it iterates. The boards return every collection as a sorted
   copy: `EnumSet`/`EnumMap` for locations and characters, `TreeSet`/`TreeMap` by id (`Ship.BY_ID`) for ships and
   centurions, because `Set.copyOf` and `Map.copyOf` keep no order, and the ops take the first ship of a kind from the
   reserves, the one with the lowest id. The Galactica view hands out no mutable object (`pilotedFighter` gives a
   `HumanFighterView`). The board interfaces have no default writers: each board implements `place` and `remove` (one
   character or one ship) and `damage` (one location) itself, so each change has exactly one public method.
7. Ships and centurions are named by their fixed ids. Decks are built in canonical order and shuffled by
   `GameOps.startGame()`.
