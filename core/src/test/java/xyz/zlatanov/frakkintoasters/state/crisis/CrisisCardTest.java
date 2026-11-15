package xyz.zlatanov.frakkintoasters.state.crisis;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CylonActivation.*;

class CrisisCardTest {

    @ParameterizedTest
    @EnumSource(CrisisCard.class)
    void shouldHaveAnActivationIcon(CrisisCard card) {
        assertNotNull(card.activation());
    }

    // An event crisis always has a chooser in this deck, and a skill check crisis has one when it has an alternate
    // instruction.
    @ParameterizedTest
    @EnumSource(CrisisCard.class)
    void shouldBeASkillCheckOrAnEventWithAChooser(CrisisCard card) {
        assertTrue(card.skillCheck() != null || card.chooser() != null);
    }

    @Test
    void shouldAlternateInstructionsBeChosenByTheCurrentPlayer() {
        assertEquals(CrisisChooser.CURRENT_PLAYER, WATER_SABOTAGED.chooser());
        assertNull(WITCH_HUNT.chooser());
        assertEquals(CrisisChooser.CAG, DETENTE.chooser());
    }

    @Test
    void shouldHaveEachSkillCheckOnceOnly() {
        val checks = Arrays.stream(CrisisCard.values()).map(CrisisCard::skillCheck).filter(java.util.Objects::nonNull).toList();
        assertEquals(checks.size(), checks.stream().distinct().count());
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

    // The Cylon Fleet crises have no jump icon: the fleet board replaces the Cylon attack cards.
    @ParameterizedTest
    @EnumSource(value = CrisisCard.class, names = {"DETENTE", "RAIDERS_INBOUND", "RETURN_TO_DUTY",
            "REVIEW_CAMERA_FOOTAGE", "TRACKED_BY_RADIATION", "TRAINING_A_ROOKIE"})
    void shouldHaveNoJumpIconOnTheCylonFleetCards(CrisisCard card) {
        assertFalse(card.prepareForJump());
    }
}
