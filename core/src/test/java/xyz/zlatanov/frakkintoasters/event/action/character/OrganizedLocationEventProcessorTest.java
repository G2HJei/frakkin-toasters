package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class OrganizedLocationEventProcessorTest extends EventTestHarness<OrganizedLocationEvent> {

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
    void letsHimActAtTheLocationThenOffersTheNext() {
        execute(new OrganizedLocationEvent(1, RESEARCH_LAB, List.of(COMMAND)));

        assertEquals(Optional.of(RESEARCH_LAB), game.turn().remoteActionLocation(1));
        assertFollowup(all(
                all(new PlayerDecisionEvent<>(1, LocationEvent.class), new EndRemoteActionEvent(1)),
                single(new OrganizedNextEvent(1, List.of(COMMAND, RESEARCH_LAB)))));
    }

    @Test
    void notALocationHeUsedAlready() {
        assertInvalid(new OrganizedLocationEvent(1, RESEARCH_LAB, List.of(RESEARCH_LAB)));
    }

    @Test
    void notADamagedLocation() {
        galacticaBoard.damage(RESEARCH_LAB);

        assertInvalid(new OrganizedLocationEvent(1, RESEARCH_LAB, List.of()));
    }

    @Test
    void notAfterThree() {
        assertInvalid(new OrganizedLocationEvent(1, RESEARCH_LAB, List.of(COMMAND, ARMORY, WEAPONS_CONTROL)));
    }
}
