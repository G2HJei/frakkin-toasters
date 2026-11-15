package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.track.BoardingParty;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// "Destroy a centurion on the Boarding Party track."
class SetATrapPassEventProcessorTest extends EventTestHarness<SetATrapPassEvent> {

    @Test
    void shouldDestroyTheCenturionClosestToTheEndOfTheTrack() {
        ops.galactica().placeCenturion();
        ops.galactica().advanceBoardingParty();
        ops.galactica().placeCenturion();

        execute(new SetATrapPassEvent());

        assertEquals(List.of(BoardingParty.START), List.copyOf(galacticaBoard.boardingPartyTrack().values()));
        assertEquals(3, cylonShips.centurions().size()); // the destroyed one is back in the supply
    }

    @Test
    void shouldDoNothingWithoutCenturions() {
        execute(new SetATrapPassEvent());

        assertEquals(0, galacticaBoard.boardingPartyTrack().size());
    }
}
