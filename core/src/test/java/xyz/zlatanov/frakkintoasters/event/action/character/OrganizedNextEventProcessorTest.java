package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class OrganizedNextEventProcessorTest extends EventTestHarness<OrganizedNextEvent> {

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
    void offersTheLocationsLeft() {
        execute(new OrganizedNextEvent(1, List.of(COMMAND)));

        assertFollowup(OrganizedEventProcessor.next(game, 1, List.of(COMMAND)));
    }

    @Test
    void endsAfterThree() {
        execute(new OrganizedNextEvent(1, List.of(COMMAND, ARMORY, RESEARCH_LAB)));
    }
}
