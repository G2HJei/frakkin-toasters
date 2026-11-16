package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_6_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;

class ReturnAssaultRaptorEventProcessorTest extends EventTestHarness<ReturnAssaultRaptorEvent> {

    @BeforeEach
    void aPilot() {
        player(2).character(LEE_APOLLO_ADAMA);
        moveTo(HANGAR_DECK, LEE_APOLLO_ADAMA);
    }

    @Test
    void thePilotGoesToTheHangarDeckAndTheRaptorToTheReserves() {
        val raptor = assaultRaptorAt(GALACTICA_SPACE_6_OCLOCK);
        raptor.pilot(LEE_APOLLO_ADAMA);

        execute(new ReturnAssaultRaptorEvent(2, raptor.id()));

        assertTrue(galacticaBoard.reserves().stream().anyMatch(ship -> ship.id() == raptor.id()));
        assertEquals(HANGAR_DECK, locate(LEE_APOLLO_ADAMA));
    }

    // The current player decides about an unmanned one, the pilot about a piloted one.
    @Test
    void anUnmannedRaptorIsReturnedByTheCurrentPlayer() {
        val raptor = assaultRaptorAt(GALACTICA_SPACE_6_OCLOCK);

        execute(new ReturnAssaultRaptorEvent(game.turn().currentPlayerNumber(), raptor.id()));

        assertTrue(galacticaBoard.reserves().stream().anyMatch(ship -> ship.id() == raptor.id()));
    }

    @Test
    void onlyThePilotDecides() {
        val raptor = assaultRaptorAt(GALACTICA_SPACE_6_OCLOCK);
        raptor.pilot(LEE_APOLLO_ADAMA);

        assertInvalid(new ReturnAssaultRaptorEvent(1, raptor.id()));
    }

    @Test
    void onlyAssaultRaptorsInSpace() {
        assertInvalid(new ReturnAssaultRaptorEvent(2, viperAt(GALACTICA_SPACE_6_OCLOCK).id()));
    }
}
