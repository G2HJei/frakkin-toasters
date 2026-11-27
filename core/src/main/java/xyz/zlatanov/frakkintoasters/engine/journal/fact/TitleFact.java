package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;

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

    // A played Quorum Card stays in play, given to a player or, for "Accept Prophecy", to nobody.
    record QuorumCardPutInPlay(QuorumCard card, Integer holderPlayerNumber) implements TitleFact {
    }

    record QuorumCardLeftPlay(QuorumCard card) implements TitleFact {
    }
}
