package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_5_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageChosenViperEventProcessorTest extends EventTestHarness<DamageChosenViperEvent> {

    @Test
    void shouldDamageAViperInTheReserves() {
        val viper = galacticaBoard.reserves().stream().filter(Viper.class::isInstance).findFirst().orElseThrow();

        execute(new DamageChosenViperEvent(1, viper.id()));

        assertTrue(galacticaBoard.damagedShips().contains(viper));
    }

    @Test
    void shouldDamageAPilotedViper() {
        player(1).character(KARA_STARBUCK_THRACE);
        val viper = viperAt(GALACTICA_SPACE_5_OCLOCK);
        ops.player(1).pilot(viper.id());

        execute(new DamageChosenViperEvent(1, viper.id()));

        assertTrue(galacticaBoard.damagedShips().contains(viper));
        assertEquals(SICKBAY, locate(KARA_STARBUCK_THRACE));
    }

    // An assault raptor can't be damaged, so it is destroyed (rule 11.3.2.3).
    @Test
    void shouldDestroyAnAssaultRaptor() {
        val assaultRaptor = assaultRaptorAt(GALACTICA_SPACE_5_OCLOCK);

        execute(new DamageChosenViperEvent(1, assaultRaptor.id()));

        assertNoShips(GALACTICA_SPACE_5_OCLOCK);
        assertFalse(galacticaBoard.damagedShips().contains(assaultRaptor));
    }

    @Test
    void shouldRejectAShipThatIsNoViper() {
        assertInvalid(new DamageChosenViperEvent(1, raiderAt(GALACTICA_SPACE_5_OCLOCK).id()));
    }

    @Test
    void shouldRejectAnUnknownShip() {
        assertInvalid(new DamageChosenViperEvent(1, -1));
    }
}
