package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHARON_BOOMER_VALERII;

class ViolentOutburstsEventProcessorTest extends EventTestHarness<ViolentOutburstsEvent> {
    @BeforeEach
    void setUp() {
        player(1).character(SHARON_BOOMER_VALERII);
        player(2).character(KARL_HELO_AGATHON);
        moveTo(ARMORY, SHARON_BOOMER_VALERII);
    }

    @Test
    void shouldSendToBrig() {
        execute(new ViolentOutburstsEvent());
        assertEquals(BRIG, game.locate(SHARON_BOOMER_VALERII));
    }

    @Test
    void shouldNotSendRevealedCylonToBrig() {
        revealCylon();
        execute(new ViolentOutburstsEvent());
        assertEquals(ARMORY, game.locate(SHARON_BOOMER_VALERII));
    }

    @Test
    void shouldNotSendUnarrivedHeloToBrig() {
        game.currentPlayerNumber(2);
        execute(new ViolentOutburstsEvent());
        assertNull(galacticaBoard.characters().get(KARL_HELO_AGATHON));
    }

    @Test
    void shouldSendHeloToBrig() {
        game.currentPlayerNumber(2).turn(2);
        execute(new ViolentOutburstsEvent());
        assertEquals(BRIG, game.locate(KARL_HELO_AGATHON));
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
        ops.player(1).pilot(fighter.id());

        execute(new ViolentOutburstsEvent());

        assertEquals(BRIG, game.locate(SHARON_BOOMER_VALERII));
        assertTrue(galacticaBoard.reserves().contains(fighter));
        assertFalse(galacticaBoard.shipsInSpace().containsKey(fighter));
        assertNull(fighter.pilot());
    }
}