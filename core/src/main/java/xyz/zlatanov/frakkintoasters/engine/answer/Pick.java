package xyz.zlatanov.frakkintoasters.engine.answer;

import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

/**
 * By position, because a hand holds equal cards.
 */
public sealed interface Pick {

    String component();

    int min();

    int max();

    default boolean ordered() {
        return false;
    }

    List<?> elements();

    /**
     * @param minStrength the strength the picked cards add up to at least, 0 when it doesn't matter ("Selfish": 20)
     */
    record SkillCardPick(String component, List<SkillCard> cards, int min, int max, boolean ordered, int minStrength)
            implements Pick {

        public SkillCardPick {
            cards = List.copyOf(cards);
        }

        @Override
        public List<SkillCard> elements() {
            return cards;
        }
    }

    record QuorumCardPick(String component, List<QuorumCard> cards, int min, int max, boolean ordered)
            implements Pick {

        public QuorumCardPick {
            cards = List.copyOf(cards);
        }

        @Override
        public List<QuorumCard> elements() {
            return cards;
        }
    }

    record ShipPick(String component, List<Integer> shipIds, int min, int max) implements Pick {

        public ShipPick {
            shipIds = List.copyOf(shipIds);
        }

        @Override
        public List<Integer> elements() {
            return shipIds;
        }
    }
}
