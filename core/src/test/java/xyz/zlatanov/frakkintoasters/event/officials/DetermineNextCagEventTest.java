package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;

class DetermineNextCagEventTest extends EventTest {

    @BeforeEach
    void setUp() {
        player(1).selectCharacter(KARA_STARBUCK_THRACE);
        player(2).selectCharacter(LEE_APOLLO_ADAMA);

        executeEvent();
    }

    @Test
    void shouldRespectLineOfSuccession() {
        assertEquals(LEE_APOLLO_ADAMA, game.cag());
    }

    @Test
    void shouldIgnoreCharactersInBrig() {
        game.moveTo(BRIG, LEE_APOLLO_ADAMA);
        executeEvent();
        assertEquals(KARA_STARBUCK_THRACE, game.cag());
    }

    @Test
    void shouldElectNoOneWhenAllAreInBrig() {
        game.moveTo(BRIG, KARA_STARBUCK_THRACE);
        game.moveTo(BRIG, LEE_APOLLO_ADAMA);
        executeEvent();
        assertNull(game.cag());
    }

    void executeEvent() {
        execute(new DetermineNextCagEvent());
    }
}