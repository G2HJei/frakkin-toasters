package xyz.zlatanov.frakkintoasters.journal.fact;

import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;

/**
 * Facts about President, Admiral and CAG.
 */
public sealed interface TitleFact extends Fact {

    record PresidentAssigned(Character character) implements TitleFact {
    }

    record AdmiralAssigned(Character character) implements TitleFact {
    }

    record PresidentQuorumCardGained(QuorumCard card) implements TitleFact {
    }

    record CagAssigned(Character character) implements TitleFact {
    }
}
