package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class DamageHumanFighterEventProcessorTest extends EventTestHarness<DamageHumanFighterEvent> {

    @Test
    void shouldDamageViperInSpace() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new DamageHumanFighterEvent(viper.id()));

        assertNoShips(GALACTICA_SPACE_4_OCLOCK);
        assertTrue(galacticaBoard.damagedShips().contains(viper));
    }

    @Test
    void shouldSendPilotToSickbay() {
        player(1).character(KARA_STARBUCK_THRACE);
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        viper.pilot(KARA_STARBUCK_THRACE);

        execute(new DamageHumanFighterEvent(viper.id()));

        assertEquals(SICKBAY, locate(KARA_STARBUCK_THRACE));
        assertNull(viper.pilot());
        assertTrue(galacticaBoard.damagedShips().contains(viper));
    }

    @Test
    void shouldDamageViperInReserves() {
        val reservesViper = new Viper(1);

        execute(new DamageHumanFighterEvent(reservesViper.id()));

        assertFalse(galacticaBoard.reserves().contains(reservesViper));
        assertTrue(galacticaBoard.damagedShips().contains(reservesViper));
    }

    @Test
    void shouldDestroyAssaultRaptor() {
        val assaultRaptor = assaultRaptorAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new DamageHumanFighterEvent(assaultRaptor.id()));

        assertNoShips(GALACTICA_SPACE_4_OCLOCK);
        assertFalse(galacticaBoard.damagedShips().contains(assaultRaptor));
        assertTrue(game.removedComponents().contains(assaultRaptor));
    }

    @Test
    void shouldNotAllowUnknownShip() {
        assertInvalid(new DamageHumanFighterEvent(99));
    }

    @Test
    void shouldNotAllowDamagingRaptor() {
        assertInvalid(new DamageHumanFighterEvent(11));
    }
}
