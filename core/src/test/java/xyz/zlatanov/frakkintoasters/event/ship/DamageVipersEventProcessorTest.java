package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent.Area.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_6_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class DamageVipersEventProcessorTest extends EventTestHarness<DamageVipersEvent> {

    // The 4 viper mark VIIs start in the "Damaged Viper" box.
    private static final int DAMAGED_AT_THE_START = 4;

    @Test
    void shouldDamageVipersInTheReserves() {
        execute(new DamageVipersEvent(2, RESERVES));

        assertEquals(DAMAGED_AT_THE_START + 2, galacticaBoard.damagedShips().size());
    }

    @Test
    void shouldDamageVipersInSpace() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new DamageVipersEvent(1, SPACE));

        assertTrue(galacticaBoard.damagedShips().contains(viper));
        assertNoShips(GALACTICA_SPACE_4_OCLOCK);
    }

    // "Damage 2 vipers in the "Reserves" (if able)."
    @Test
    void shouldDamageAsManyAsThereAre() {
        val inReserves = galacticaBoard.reserves().stream().filter(Viper.class::isInstance).count();

        execute(new DamageVipersEvent((int) inReserves + 2, RESERVES));

        assertEquals(DAMAGED_AT_THE_START + inReserves, galacticaBoard.damagedShips().size());
    }

    // Assault raptors can't be damaged (rule 11.3.2.3).
    @Test
    void shouldNotDamageAnAssaultRaptor() {
        val raptor = assaultRaptorAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new DamageVipersEvent(1, SPACE));

        assertEquals(GALACTICA_SPACE_4_OCLOCK, locate(raptor));
    }

    @Test
    void shouldDamageUnmannedVipersBeforePilotedOnes() {
        player(1).character(KARA_STARBUCK_THRACE);
        val piloted = viperAt(GALACTICA_SPACE_4_OCLOCK);
        ops.player(1).pilot(piloted.id());
        val unmanned = viperAt(GALACTICA_SPACE_6_OCLOCK);

        execute(new DamageVipersEvent(1, SPACE));

        assertTrue(galacticaBoard.damagedShips().contains(unmanned));
        assertEquals(GALACTICA_SPACE_4_OCLOCK, locate(piloted));
    }

    // Both: the "Reserves" come first.
    @Test
    void shouldDamageTheReservesBeforeSpaceWhenEitherWillDo() {
        val inSpace = viperAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new DamageVipersEvent(1, SPACE_OR_RESERVES));

        assertEquals(GALACTICA_SPACE_4_OCLOCK, locate(inSpace));
        assertEquals(DAMAGED_AT_THE_START + 1, galacticaBoard.damagedShips().size());
    }
}
