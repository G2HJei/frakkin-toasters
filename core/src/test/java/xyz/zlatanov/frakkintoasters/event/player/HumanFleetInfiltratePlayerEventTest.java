package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HUMAN_FLEET;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAPRICA_SIX;

class HumanFleetInfiltratePlayerEventTest {

    Game game = Game.builder().build();

    @Test
    void shouldBeginInfiltration() {
        game.player(1).selectCharacter(CAPRICA_SIX);
        game.moveTo(HUMAN_FLEET, CAPRICA_SIX);

        new HumanFleetInfiltratePlayerEvent(1, RESEARCH_LAB).execute(game);

        assertTrue(game.player(1).isInfiltrating());
        assertEquals(RESEARCH_LAB, game.locate(CAPRICA_SIX));
    }
}