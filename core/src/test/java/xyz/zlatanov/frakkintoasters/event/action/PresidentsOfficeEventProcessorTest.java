package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

class PresidentsOfficeEventProcessorTest extends EventTestHarness<PresidentsOfficeEvent> {

    @Test
    void shouldDraw2PoliticsCards() {
        execute(new PresidentsOfficeEvent(1));
        assertNoFollowup();
        assertHas2PoliticsCards();
    }

    private void assertHas2PoliticsCards() {
        val colorOfCards = player(1)
                .skillCards().cards()
                .stream()
                .map(c -> c.type().color())
                .toList();
        assertEquals(List.of(POLITICS, POLITICS), colorOfCards);
    }
}