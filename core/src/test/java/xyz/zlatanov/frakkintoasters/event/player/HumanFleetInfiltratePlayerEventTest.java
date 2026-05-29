package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HUMAN_FLEET;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAPRICA_SIX;

class HumanFleetInfiltratePlayerEventTest extends EventTest {

    @Test
    void shouldBeginInfiltration() {
        player(1).selectCharacter(CAPRICA_SIX);
        game.moveTo(HUMAN_FLEET, CAPRICA_SIX);

        execute(new HumanFleetInfiltratePlayerEvent(1, RESEARCH_LAB));

        assertTrue(game.player(1).isInfiltrating());
        assertEquals(RESEARCH_LAB, game.locate(CAPRICA_SIX));
    }
}