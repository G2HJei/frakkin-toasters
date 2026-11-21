package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.FELIX_GAETA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class MoveFromBrigEventProcessorTest extends EventTestHarness<MoveFromBrigEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(FELIX_GAETA);
        moveTo(BRIG, KARA_STARBUCK_THRACE);
        moveTo(FTL_CONTROL, FELIX_GAETA);
    }

    @Test
    void shouldMoveTheCharacterOutOfTheBrig() {
        execute(new MoveFromBrigEvent(1, COMMAND));

        assertEquals(COMMAND, locate(KARA_STARBUCK_THRACE));
    }

    // The Admiral is in the "Brig" only when every human was: the one who leaves takes the title (rule 4.4.2.3).
    @Test
    void shouldHandTheAdmiralsTitleToTheOneWhoLeaves() {
        moveTo(BRIG, FELIX_GAETA);
        ops.titles().assignAdmiral(FELIX_GAETA);

        execute(new MoveFromBrigEvent(1, COMMAND));

        assertFollowup(new DetermineNextAdmiralEvent());
    }

    @Test
    void shouldNotMoveSomeoneWhoIsNotInTheBrig() {
        assertInvalid(new MoveFromBrigEvent(2, COMMAND));
    }

    @Test
    void shouldNotMoveToAnotherBoard() {
        assertInvalid(new MoveFromBrigEvent(1, PEGASUS_CIC));
    }

    // The card that moves the character decides how far: "Make a Deal" allows "Sickbay", "Presidential Pardon" doesn't.
    @Test
    void shouldMoveToAHazardousLocationACardAllows() {
        execute(new MoveFromBrigEvent(1, SICKBAY));

        assertEquals(SICKBAY, locate(KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldNotMoveBackToTheBrig() {
        assertInvalid(new MoveFromBrigEvent(1, BRIG));
    }

    @Test
    void shouldNotMoveSomebodyWhoDoesNotExist() {
        assertInvalid(new MoveFromBrigEvent(9, COMMAND));
    }
}
