package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class CoupEventProcessorTest extends EventTestHarness<CoupEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(FELIX_GAETA);
        player(2).character(WILLIAM_ADAMA);
        moveTo(FTL_CONTROL, FELIX_GAETA);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
        cag(WILLIAM_ADAMA);
    }

    @Test
    void takesTheAdmiralTitle() {
        execute(new CoupEvent(1));

        assertEquals(FELIX_GAETA, admiral());
        assertEquals(0, player(1).miracleTokens());
    }

    @Test
    void fromTheBrigHeMovesToCommandFirst() {
        moveTo(BRIG, FELIX_GAETA);

        execute(new CoupEvent(1));

        assertEquals(COMMAND, locate(FELIX_GAETA));
        assertEquals(FELIX_GAETA, admiral());
    }

    @Test
    void notWhenHeIsAdmiral() {
        admiral(FELIX_GAETA);

        assertInvalid(new CoupEvent(1));
    }
}
