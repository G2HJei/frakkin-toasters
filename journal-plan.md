# Journal plan

Implements the journal described in `refactor-game-view.md`. The journal records exactly what the game ops did, so a
game can be replayed from the journal alone.

## Replay model

**Fact-sourced replay.** The journal holds facts with the full result data. Replaying means applying the facts to a
fresh `Game`. It never re-runs processors and never needs randomness. The alternative, event-sourced replay (re-run
events with recorded inputs and a seed), needs every processor to be deterministic and doesn't use the facts at all.
Fact-sourced replay matches item 7 of the design doc: each op builds a fact and applies it through one `apply(fact)`, so
live play and replay share one code path.

Rules that follow from it:

1. **A fact applies exactly what it says and nothing more.** No cascades. `LocationDamaged` marks the damage, and the
   Sickbay moves are separate `CharacterMoved` facts logged under it.
2. **Every random or looked-up result goes into the fact**: drawn cards, the new order after a shuffle, die values, ids
   of ships taken from the supply.
3. **Only `apply` writes to components.** Ops compute a fact from the current state, then call `record(fact)`, which
   applies it and appends it.

## Phase 1 - Journal skeleton (`journal/` package)

- `Fact`: sealed interface. Records grouped by area under
  `journal/fact/{player,ship,deck,galactica,skillcheck,title,game}`.
- `Journal`: append-only list of `JournalEntry`: `ScopeOpened(scopeId, parentId, event, actor)`,
  `FactRecorded(scopeId, fact)`, `ScopeClosed`. Flat, so it is easy to serialize later. It has a read API (entries,
  facts
  of a scope). Ops get a narrower `JournalWriter`.
- `AbstractOps` gets the journal (resolves its `//todo journal`). `Game` owns the `Journal`. `GameOps` and `TestHarness`
  create it.
- `FactApplier`: one exhaustive `switch` over `Fact`. The only place that calls component write methods.
- Scope open/close is the engine's job later. Until then `EventTestHarness.execute` opens a scope.

## Phase 2 - Make the state replayable

Gaps in the current code:

- **Randomness.** `Deck.shuffle()` uses `Collections.shuffle`, `Die.roll()` uses `new Random()`, and `Deck.draw()`
  reshuffles silently.
    - Add an injectable, seedable `Randomness` (replaces `Die`'s `todo make deterministic`).
    - A shuffle becomes the op `shuffle(deckId)`, which records `DeckShuffled(deckId, newDrawOrder)`. Applying it sets
      the
      draw pile to that order and clears the discard pile.
    - `Deck.draw()` no longer reshuffles on its own. The op logs a `DeckShuffled` fact when a draw empties the pile
      (`playFromDestinyDeck` already does this by hand).
- **Deck identity.** Add a `DeckId` enum and a `DecksHolder.deck(DeckId)` lookup so facts can name a deck.
- **Initial state.** `AllCardsProvider` builds shuffled decks in constructors, outside any journal. Build decks in
  canonical order and let setup ops shuffle them (logged). The journal starts with
  `GameStarted(playerCount, objective)`.
  `Game.setupGalacticaBoard()` moves into ops.
- **State changes outside ops.** `step`, `turn`, `currentPlayerNumber`, `president/admiral/cag` and
  `actingPlayerNumber` are written directly. Give them ops and facts: `StepChanged`, `TurnStarted`,
  `CurrentPlayerChanged`, `ActingPlayerChanged`.
- **Ids.** Verify ship ids are assigned in a fixed creation order, before any shuffle. Then
  `RaiderLaunched(id, from, to)`
  replays to the same ship.
- **Motive cards.** `PlayerOps.drawMotiveCards()` draws from a fresh `genericDeck(MotiveCard.class)` that isn't in
  `Game`. Put a motive deck into `DecksHolder`, otherwise replay can't reconstruct it.

## Phase 3 - Convert the ops, one class at a time

Order: `PlayerOps` -> `ShipOps` -> `GalacticaOps` / `PegasusOps` / `BattlestarOps` -> `SkillCheckOps` ->
`CylonFleetOps` -> `LoyaltyDeckOps` -> `TitleOps` -> `GameOps` (`rollDie` -> `DieRolled`, acting player).

For each class:

- Split composite methods into primitive facts. In `ShipOps`:
    - `damage()` branches three ways (reserves, basestar, fighter). It becomes `ShipMovedToReserves`,
      `BasestarDamageCardAttached(card)`, `ShipMovedToDamaged`, `ShipRemovedFromGame`, `PilotCleared`.
    - Destroying a basestar becomes `BasestarDamageDiscarded` + `CylonShipReturned`. A civilian ship's cost becomes
      `ResourceDecreased(kind, amount)`.
    - `damageLocation` becomes `LocationDamaged` followed by `CharacterMoved(-> SICKBAY)` facts.
- Fact fields are immutable and complete. Cards drawn are logged in full. Hiding them is the describers' job.
- Component methods with hidden side effects (the `Deck.draw` reshuffle, `CylonShips.returned` clearing damage) get
  split
  or report what they did.
- Ops that return values keep returning immutable values (card lists, ids).
- Existing op tests (`ShipOpsDestroyTest`, `PlayerOpsDrawSkillCardTest`, ...) gain assertions on the logged facts.

## Phase 4 - Replayer and verification

- `GameReplayer.replay(Journal) -> Game`: reads `GameStarted`, builds the `Game`, applies each `FactRecorded` through
  `FactApplier`, ignores scope entries.
- Acceptance test: a scripted scenario of mixed ops (setup shuffles, draws, a skill check, ship damage and destruction,
  a
  die roll). Assert the replayed `Game` equals the live one. This needs a state-comparison helper, since only `Deck` has
  `@EqualsAndHashCode` today. A `GameSnapshot` record or a test-side deep comparison would do.
- Per-fact tests: apply the fact to a prepared state and check the result.
- Seed test: replaying a journal gives the same end state regardless of the `Randomness` seed of the replaying game.

## Phase 5 - Guard rails (optional, cheap)

- `@Mutates` on component write methods plus ArchUnit rules: only `..operation..` and tests may call them, `..event..`
  may not. ArchUnit isn't in `core/build.gradle` yet. This keeps the log complete, which matters because replay breaks
  silently otherwise.
- Update `CLAUDE.md` (journal and replay rules) and tick roadmap step 3.

## Not in this plan

- Describers and per-viewer hiding. Only the `Fact` types they will consume are defined here.
- Journal serialization (JSON). The records are plain, so it can be added later.
- The engine, the pending followup queue and reconnects. A fact-only journal rebuilds state but not unresolved
  followups, which belong to roadmap step 4.
- The "airtight GameView" work (item 1 of the design doc). It is independent of this.

## Assumptions

1. Fact-sourced replay, as described above.
2. Decks are built unshuffled and shuffled by logged setup ops. This changes the default `Game.builder()` order, so
   tests
   that rely on random order may need `FakeDeck`.
3. One `Journal` per `Game`, in memory only for now.
