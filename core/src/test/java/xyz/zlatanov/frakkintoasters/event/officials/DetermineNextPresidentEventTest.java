package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class DetermineNextPresidentEventTest {

    Game game = Game.builder(2).build();

    @BeforeEach
    void setUp() {
        game.player(1).selectCharacter(GAIUS_BALTAR);
        game.player(2).selectCharacter(LAURA_ROSLIN);

        new DetermineNextPresidentEvent().execute(game);
    }

    @Test
    void shouldRespectLineOfSuccession() {
        assertEquals(LAURA_ROSLIN, game.president());
    }

    @Test
    void shouldDistributeQuorumCard() {
        assertEquals(1, game.presidentHand().size());
    }
}