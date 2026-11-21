package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class DeclareMartialLawEventProcessorTest extends EventTestHarness<DeclareMartialLawEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(SAUL_TIGH);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        moveTo(COMMAND, SAUL_TIGH);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        president(LAURA_ROSLIN);
    }

    @Test
    void theAdmiralBecomesPresident() {
        execute(new DeclareMartialLawEvent(1));

        assertFollowup(new MakePresidentEvent(WILLIAM_ADAMA));
    }

    @Test
    void notWhenTheAdmiralIsPresident() {
        president(WILLIAM_ADAMA);

        assertInvalid(new DeclareMartialLawEvent(1));
    }
}
