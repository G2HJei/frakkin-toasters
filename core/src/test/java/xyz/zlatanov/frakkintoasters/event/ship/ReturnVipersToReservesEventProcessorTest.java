package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class ReturnVipersToReservesEventProcessorTest extends EventTestHarness<ReturnVipersToReservesEvent> {

    @Test
    void shouldReturnUnmannedVipers() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new ReturnVipersToReservesEvent());

        assertTrue(galacticaBoard.reserves().contains(viper));
        assertNoShips(GALACTICA_SPACE_4_OCLOCK);
    }

    // "All characters who were piloting vipers are placed in the "Hangar Deck.""
    @Test
    void shouldPlacePilotsOnTheHangarDeck() {
        player(1).character(KARA_STARBUCK_THRACE);
        val viper = viperAt(GALACTICA_SPACE_6_OCLOCK);
        ops.player(1).pilot(viper.id());

        execute(new ReturnVipersToReservesEvent());

        assertTrue(galacticaBoard.reserves().contains(viper));
        assertEquals(HANGAR_DECK, locate(KARA_STARBUCK_THRACE));
    }

    // Assault raptors count as vipers (rule 11.3.2.2).
    @Test
    void shouldReturnAssaultRaptorsToo() {
        val raptor = assaultRaptorAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new ReturnVipersToReservesEvent());

        assertTrue(galacticaBoard.reserves().contains(raptor));
    }

    @Test
    void shouldLeaveOtherShipsAlone() {
        val civilianShip = civilianShipAt(GALACTICA_SPACE_2_OCLOCK);
        val raider = raiderAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new ReturnVipersToReservesEvent());

        assertEquals(GALACTICA_SPACE_2_OCLOCK, locate(civilianShip));
        assertEquals(GALACTICA_SPACE_4_OCLOCK, locate(raider));
    }
}
