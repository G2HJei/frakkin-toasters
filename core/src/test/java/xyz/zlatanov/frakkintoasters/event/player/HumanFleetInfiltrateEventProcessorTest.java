package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HUMAN_FLEET;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAPRICA_SIX;

class HumanFleetInfiltrateEventProcessorTest extends EventTestHarness<HumanFleetInfiltrateEvent> {

    @Test
    void shouldBeginInfiltration() {
        player(1).character(CAPRICA_SIX);
        moveTo(HUMAN_FLEET, CAPRICA_SIX);

        execute(new HumanFleetInfiltrateEvent(1, RESEARCH_LAB));

        assertTrue(player(1).isInfiltrating());
        assertEquals(RESEARCH_LAB, locate(CAPRICA_SIX));
    }
}