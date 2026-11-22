package xyz.zlatanov.frakkintoasters.engine.answer;

import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

/**
 * The component of a shaped answer that the player fills: between {@code min} and {@code max} of the elements it
 * lists, by position (a hand holds equal cards), in the order he puts them or in any order. A pick on a component that
 * holds a single element, such as a move's discard, has {@code min = max = 1}.
 */
public sealed interface Pick {

    String component();

    int min();

    int max();

    default boolean ordered() {
        return false;
    }

    /**
     * @return the elements to pick from
     */
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
