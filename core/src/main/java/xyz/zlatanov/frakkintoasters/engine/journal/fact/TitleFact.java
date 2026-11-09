package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;

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

    record PresidentQuorumCardsLost(List<QuorumCard> cards) implements TitleFact {
        public PresidentQuorumCardsLost {
            cards = List.copyOf(cards);
        }
    }

    record CagAssigned(Character character) implements TitleFact {
    }
}
