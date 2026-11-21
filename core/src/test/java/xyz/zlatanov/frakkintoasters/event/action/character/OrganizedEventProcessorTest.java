package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class OrganizedEventProcessorTest extends EventTestHarness<OrganizedEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LOUIS_HOSHI);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        moveTo(COMMUNICATIONS, LOUIS_HOSHI);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    @Test
    void offersTheLocationsHeCanActAtFromAnywhere() {
        execute(new OrganizedEvent(1));

        val locations = RemoteActions.locations(game, player(1), false);
        assertFalse(locations.contains(BRIG));
        assertFalse(locations.contains(CAPRICA));
        assertEquals(0, player(1).miracleTokens());
        assertFollowup(OrganizedEventProcessor.next(game, 1, List.of()));
    }

    @Test
    void notFromTheBrig() {
        moveTo(BRIG, LOUIS_HOSHI);

        assertInvalid(new OrganizedEvent(1));
    }
}
