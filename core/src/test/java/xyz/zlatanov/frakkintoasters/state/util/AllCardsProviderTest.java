package xyz.zlatanov.frakkintoasters.state.util;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

// The Skill decks have the strengths Appendix A2.3.8 lists.
class AllCardsProviderTest {

    private static final Map<Integer, Integer> COLOR_STRENGTHS = Map.of(0, 5, 1, 9, 2, 7, 3, 6, 4, 4, 5, 3, 6, 1);
    private static final Map<Integer, Integer> TREACHERY_STRENGTHS = Map.of(0, 12, 3, 8, 4, 3, 5, 3);

    @Test
    void eachColorHasTheStrengthsOfTheAppendix() {
        List<Supplier<Deck<SkillCard>>> colors = List.of(AllCardsProvider::politicsCards, AllCardsProvider::leadershipCards,
                AllCardsProvider::tacticsCards, AllCardsProvider::pilotingCards, AllCardsProvider::engineeringCards);
        for (val color : colors) {
            assertEquals(new TreeMap<>(COLOR_STRENGTHS), strengths(color.get()));
        }
    }

    @Test
    void treacheryHasTheStrengthsOfTheAppendix() {
        assertEquals(new TreeMap<>(TREACHERY_STRENGTHS), strengths(AllCardsProvider.treacheryCards()));
    }

    @Test
    void thereAre201SkillCards() {
        val total = AllCardsProvider.politicsCards().size() + AllCardsProvider.leadershipCards().size()
                + AllCardsProvider.tacticsCards().size() + AllCardsProvider.pilotingCards().size()
                + AllCardsProvider.engineeringCards().size() + AllCardsProvider.treacheryCards().size();
        assertEquals(201, total);
    }

    private static Map<Integer, Integer> strengths(Deck<SkillCard> deck) {
        val counts = new TreeMap<Integer, Integer>();
        deck.cards().forEach(card -> counts.merge(card.value(), 1, Integer::sum));
        return counts;
    }
}
