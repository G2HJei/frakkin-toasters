package xyz.zlatanov.frakkintoasters.event.deck;

import static org.junit.jupiter.api.Assertions.assertTrue;
import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;

class CreateDestinyDeckEventProcessorTest extends EventTestHarness<CreateDestinyDeckEvent> {

    @Test
    void shouldCreateValidDeck() {
        execute(new CreateDestinyDeckEvent());
        assertDestinyDeckComposition();
    }

    private void assertDestinyDeckComposition() {
        val expectedDistribution = Map.of(
                POLITICS, 2,
                LEADERSHIP, 2,
                TACTICS, 2,
                PILOTING, 2,
                ENGINEERING, 2,
                TREACHERY, 2);
        val actualDistribution = destinyDeck.cards()
                .stream()
                .collect(groupingBy(
                        card -> card.type().color(),
                        summingInt(e -> 1)
                ));
        assertEquals(expectedDistribution, actualDistribution);
    }

    // A color with no cards left can't be drawn (rule 8.2.2.5, F-9f4ada).
    @Test
    void leavesOutAColorWithNoCardsLeft() {
        politicsDeck.clear();

        execute(new CreateDestinyDeckEvent());

        assertEquals(10, destinyDeck.cards().size());
        assertTrue(destinyDeck.cards().stream().noneMatch(card -> card.type().color() == POLITICS));
    }
}
