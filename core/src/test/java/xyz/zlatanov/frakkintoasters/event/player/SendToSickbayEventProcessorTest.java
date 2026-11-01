package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;

class SendToSickbayEventProcessorTest extends EventTestHarness<SendToSickbayEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(LEE_APOLLO_ADAMA);
    }

    @Test
    void shouldSendTargetPlayerToSickbay() {
        moveTo(COMMAND, LEE_APOLLO_ADAMA);

        execute(new SendToSickbayEvent(1, 2));

        assertEquals(SICKBAY, locate(LEE_APOLLO_ADAMA));
    }

    @Test
    void shouldLandPilotedViper() {
        assertLandedInReserves(viperAt(GALACTICA_SPACE_2_OCLOCK));
    }

    @Test
    void shouldLandPilotedViperMarkVII() {
        assertLandedInReserves(viperMarkVIIAt(GALACTICA_SPACE_2_OCLOCK));
    }

    @Test
    void shouldLandPilotedAssaultRaptor() {
        assertLandedInReserves(assaultRaptorAt(GALACTICA_SPACE_2_OCLOCK));
    }

    private void assertLandedInReserves(HumanFighter fighter) {
        ops.player(2).pilot(fighter.id());

        execute(new SendToSickbayEvent(1, 2));

        assertEquals(SICKBAY, locate(LEE_APOLLO_ADAMA));
        assertTrue(galacticaBoard.reserves().contains(fighter));
        assertFalse(galacticaBoard.shipsInSpace().containsKey(fighter));
        assertNull(fighter.pilot());
    }
}
