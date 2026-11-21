package xyz.zlatanov.frakkintoasters.event.ship;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.HELENA_CAIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class DestroyPegasusEventProcessorTest extends EventTestHarness<DestroyPegasusEvent> {

    // Everyone on Pegasus goes to "Sickbay" and its board leaves the game (rule 11.7.2.4). A character may stand in a
    // damaged location.
    @Test
    void shouldMoveEveryoneOnPegasusToSickbay() {
        player(1).character(HELENA_CAIN);
        player(2).character(WILLIAM_ADAMA);
        pegasusBoard.damage(PEGASUS_CIC).damage(MAIN_BATTERIES).damage(ENGINE_ROOM).damage(AIRLOCK);
        moveTo(ENGINE_ROOM, HELENA_CAIN);
        moveTo(COMMAND, WILLIAM_ADAMA);

        execute(new DestroyPegasusEvent());

        assertTrue(pegasusBoard.destroyed());
        assertEquals(SICKBAY, locate(HELENA_CAIN));
        assertEquals(COMMAND, locate(WILLIAM_ADAMA));
    }

    @Test
    void shouldRejectAPegasusWithAnUndamagedLocation() {
        pegasusBoard.damage(PEGASUS_CIC).damage(MAIN_BATTERIES).damage(ENGINE_ROOM);

        assertInvalid(new DestroyPegasusEvent());
    }

    @Test
    void shouldRejectAPegasusDestroyedAlready() {
        pegasusBoard.damage(PEGASUS_CIC).damage(MAIN_BATTERIES).damage(ENGINE_ROOM).damage(AIRLOCK).destroy();

        assertInvalid(new DestroyPegasusEvent());
    }
}
