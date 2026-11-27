package xyz.zlatanov.frakkintoasters.engine.journal.fact;

/**
 * A fact applies exactly what it says and nothing more (no cascades); every random or looked-up result is part of the
 * fact; only {@code FactApplier} writes to components.
 * <p>
 * The records live in this package because a sealed hierarchy can't span packages outside a named module.
 */
public sealed interface Fact permits BoardFact, DeckFact, GameFact, InterruptFact, PlayerFact, ShipFact, SkillCheckFact,
        TitleFact {
}
