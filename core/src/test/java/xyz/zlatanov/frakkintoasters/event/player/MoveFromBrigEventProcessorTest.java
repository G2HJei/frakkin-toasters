package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

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

    @Test
    void shouldNotMoveSomeoneWhoIsNotInTheBrig() {
        assertInvalid(new MoveFromBrigEvent(2, COMMAND));
    }

    @Test
    void shouldNotMoveToAnotherBoard() {
        assertInvalid(new MoveFromBrigEvent(1, PEGASUS_CIC));
    }

    @Test
    void shouldNotMoveToAHazardousLocation() {
        assertInvalid(new MoveFromBrigEvent(1, SICKBAY));
    }

    @Test
    void shouldNotMoveSomebodyWhoDoesNotExist() {
        assertInvalid(new MoveFromBrigEvent(9, COMMAND));
    }
}
