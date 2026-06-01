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
        selectCharacter(1, KARA_STARBUCK_THRACE);
        selectCharacter(2, LEE_APOLLO_ADAMA);

        executeAndAssertNoFollowup(new DetermineNextCagEvent());
    }

    @Test
    void shouldRespectLineOfSuccession() {
        assertEquals(LEE_APOLLO_ADAMA, cag());
    }

    @Test
    void shouldIgnoreCharactersInBrig() {
        moveTo(BRIG, LEE_APOLLO_ADAMA);
        executeAndAssertNoFollowup(new DetermineNextCagEvent());
        assertEquals(KARA_STARBUCK_THRACE, cag());
    }

    @Test
    void shouldElectNoOneWhenAllAreInBrig() {
        moveTo(BRIG, KARA_STARBUCK_THRACE);
        moveTo(BRIG, LEE_APOLLO_ADAMA);
        executeAndAssertNoFollowup(new DetermineNextCagEvent());
        assertNull(cag());
    }

}