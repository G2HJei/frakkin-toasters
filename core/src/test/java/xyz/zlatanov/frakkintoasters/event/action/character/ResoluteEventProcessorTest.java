package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ResoluteEventProcessorTest extends EventTestHarness<ResoluteEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(SHARON_ATHENA_AGATHON);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        moveTo(RESURRECTION_SHIP, SHARON_ATHENA_AGATHON);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    @Test
    void takesTheActionOfALocationFromAnywhere() {
        execute(new ResoluteEvent(1, RESEARCH_LAB));

        assertEquals(Optional.of(RESEARCH_LAB), game.turn().remoteActionLocation(1));
        assertFollowup(all(new PlayerDecisionEvent<>(1, LocationEvent.class), new EndRemoteActionEvent(1)));
    }

    @Test
    void notADamagedLocation() {
        galacticaBoard.damage(RESEARCH_LAB);

        assertInvalid(new ResoluteEvent(1, RESEARCH_LAB));
    }

    @Test
    void notTheBrig() {
        assertInvalid(new ResoluteEvent(1, BRIG));
    }
}
