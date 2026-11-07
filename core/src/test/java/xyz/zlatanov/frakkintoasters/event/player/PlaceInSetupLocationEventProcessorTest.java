package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.DANNA_BIERS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.HELENA_CAIN;

class PlaceInSetupLocationEventProcessorTest extends EventTestHarness<PlaceInSetupLocationEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(HELENA_CAIN);
    }

    @Test
    void shouldPlaceCharacterOnChosenSetupLocation() {
        execute(new PlaceInSetupLocationEvent(1, PEGASUS_CIC));
        assertEquals(PEGASUS_CIC, locate(HELENA_CAIN));
    }

    @Test
    void shouldPlaceCylonLeaderOnChosenSetupLocation() {
        player(2).character(DANNA_BIERS);
        execute(new PlaceInSetupLocationEvent(2, HUMAN_FLEET));
        assertEquals(HUMAN_FLEET, locate(DANNA_BIERS));
    }

    @Test
    void shouldNotPlaceOutsideTheSetupLocations() {
        assertInvalid(new PlaceInSetupLocationEvent(1, ARMORY));
    }

    @Test
    void shouldNotPlaceCharacterAlreadyOnTheBoard() {
        moveTo(COMMAND, HELENA_CAIN);
        assertInvalid(new PlaceInSetupLocationEvent(1, PEGASUS_CIC));
    }
}
