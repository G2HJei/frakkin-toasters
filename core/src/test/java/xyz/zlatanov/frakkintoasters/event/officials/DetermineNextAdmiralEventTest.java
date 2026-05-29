package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.HELENA_CAIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class DetermineNextAdmiralEventTest extends EventTest {

    @BeforeEach
    void setUp() {
        player(1).selectCharacter(WILLIAM_ADAMA);
        player(2).selectCharacter(HELENA_CAIN);

        executeEvent();
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
        executeEvent();
        assertEquals(WILLIAM_ADAMA, game.admiral());
    }

    @Test
    void shouldElectNoOneWhenAllAreInBrig() {
        game.moveTo(BRIG, HELENA_CAIN);
        game.moveTo(BRIG, WILLIAM_ADAMA);
        executeEvent();
        assertNull(game.admiral());
    }

    void executeEvent() {
        execute(new DetermineNextAdmiralEvent());
    }
}