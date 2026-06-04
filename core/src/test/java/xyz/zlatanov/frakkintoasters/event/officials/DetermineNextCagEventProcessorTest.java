package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;

class DetermineNextCagEventProcessorTest extends EventTestHarness<DetermineNextCagEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(LEE_APOLLO_ADAMA);
    }

    @Test
    void shouldRespectLineOfSuccession() {
        execute(new DetermineNextCagEvent());
        assertEquals(LEE_APOLLO_ADAMA, cag());
    }

    @Test
    void shouldIgnoreCharactersInBrig() {
        moveTo(BRIG, LEE_APOLLO_ADAMA);
        execute(new DetermineNextCagEvent());
        assertEquals(KARA_STARBUCK_THRACE, cag());
    }

    @Test
    void shouldElectNoOneWhenAllAreInBrig() {
        moveTo(BRIG, KARA_STARBUCK_THRACE);
        moveTo(BRIG, LEE_APOLLO_ADAMA);

        execute(new DetermineNextCagEvent());

        assertNull(cag());
    }

}