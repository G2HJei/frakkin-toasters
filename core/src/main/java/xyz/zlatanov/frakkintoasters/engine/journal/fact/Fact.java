package xyz.zlatanov.frakkintoasters.engine.journal.fact;

/**
 * One thing a game op did to the state, with the full result data. Applying the facts of a journal to a fresh game
 * rebuilds the game without re-running processors or needing randomness.
 * <p>
 * Rules: a fact applies exactly what it says and nothing more (no cascades); every random or looked-up result is part
 * of the fact; only {@code FactApplier} writes to components.
 * <p>
 * The records are grouped by area in the nested types of the area interfaces ({@link GameFact}, {@link TitleFact}, ...).
 * They all live in this package because a sealed hierarchy can't span packages outside a named module.
 */
public sealed interface Fact permits BoardFact, DeckFact, GameFact, PlayerFact, ShipFact, SkillCheckFact, TitleFact {
}
