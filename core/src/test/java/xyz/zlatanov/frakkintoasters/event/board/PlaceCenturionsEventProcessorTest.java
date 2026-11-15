package xyz.zlatanov.frakkintoasters.event.board;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.START;

class PlaceCenturionsEventProcessorTest extends EventTestHarness<PlaceCenturionsEvent> {

    @Test
    void shouldPlaceCenturionsAtTheStartOfTheBoardingPartyTrack() {
        execute(new PlaceCenturionsEvent(2));

        assertEquals(2, galacticaBoard.boardingPartyTrack().size());
        assertEquals(1, galacticaBoard.boardingPartyTrack().values().stream().distinct().count());
        assertEquals(START, galacticaBoard.boardingPartyTrack().values().iterator().next());
    }

    // A centurion that isn't left isn't placed (rule 13.4).
    @Test
    void shouldPlaceOnlyTheCenturionsThatAreLeft() {
        cylonShips.centurion();
        cylonShips.centurion();
        cylonShips.centurion();

        execute(new PlaceCenturionsEvent(2));

        assertEquals(1, galacticaBoard.boardingPartyTrack().size());
    }
}
