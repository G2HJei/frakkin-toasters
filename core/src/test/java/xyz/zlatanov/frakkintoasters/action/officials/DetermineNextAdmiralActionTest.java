package xyz.zlatanov.frakkintoasters.action.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.HELENA_CAIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class DetermineNextAdmiralActionTest {
    Game game = new Game(KOBOL, 2);

    @BeforeEach
    void setUp() {
        game.player(1).selectCharacter(WILLIAM_ADAMA);
        game.player(2).selectCharacter(HELENA_CAIN);

        new DetermineNextAdmiralAction().execute(game);
    }

    @Test
    void shouldRespectLineOfSuccession() {
        assertEquals(HELENA_CAIN, game.admiral());
    }

    @Test
    void shouldStartWith2Nukes() {
        assertEquals(2, game.nukes());
    }

    @Test
    void shouldIgnoreCharactersInBrig() {
        game.moveTo(BRIG, HELENA_CAIN);
        new DetermineNextAdmiralAction().execute(game);
        assertEquals(WILLIAM_ADAMA, game.admiral());
    }

    @Test
    void shouldElectNoOneWhenAllAreInBrig() {
        game.moveTo(BRIG, HELENA_CAIN);
        game.moveTo(BRIG, WILLIAM_ADAMA);
        new DetermineNextAdmiralAction().execute(game);
        assertNull(game.admiral());
    }
}