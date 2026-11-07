package xyz.zlatanov.frakkintoasters.state.crisis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CylonActivation.*;

class CrisisCardTest {

    @ParameterizedTest
    @EnumSource(CrisisCard.class)
    void shouldHaveAnActivationIconWhenItHasAChooser(CrisisCard card) {
        assertEquals(card.chooser() != null, card.activation() != null);
    }

    @Test
    void shouldTellTheRiotsCardsApart() {
        assertEquals(RIOTS_JUMP.chooser(), RIOTS_NO_JUMP.chooser());
        assertEquals(ACTIVATE_BASESTARS, RIOTS_JUMP.activation());
        assertEquals(LAUNCH_RAIDERS, RIOTS_NO_JUMP.activation());
        assertTrue(RIOTS_JUMP.prepareForJump());
        assertFalse(RIOTS_NO_JUMP.prepareForJump());
    }

    @Test
    void shouldTellTheWaterShortageCardsApart() {
        assertEquals(ACTIVATE_BASESTARS, WATER_SHORTAGE_BASESTAR_AND_JUMP.activation());
        assertTrue(WATER_SHORTAGE_BASESTAR_AND_JUMP.prepareForJump());
        assertEquals(ACTIVATE_BASESTARS, WATER_SHORTAGE_BASESTAR_NO_JUMP.activation());
        assertFalse(WATER_SHORTAGE_BASESTAR_NO_JUMP.prepareForJump());
        assertEquals(ACTIVATE_RAIDERS, WATER_SHORTAGE_RAIDERS.activation());
        assertTrue(WATER_SHORTAGE_RAIDERS.prepareForJump());
    }

    @Test
    void shouldTellTheRescueMissionCardsApart() {
        assertEquals(ACTIVATE_BASESTARS, RESCUE_MISSION_BASESTAR.activation());
        assertEquals(ACTIVATE_RAIDERS, RESCUE_MISSION_RAIDER.activation());
    }
}
