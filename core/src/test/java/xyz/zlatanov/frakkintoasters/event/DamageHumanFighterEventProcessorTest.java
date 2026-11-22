package xyz.zlatanov.frakkintoasters.event;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import static xyz.zlatanov.frakkintoasters.state.character.Character.BRENDAN_HOTDOG_COSTANZA;
import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
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
        val viper = galacticaBoard.removeFromReserves(Viper.class).orElseThrow();
        galacticaBoard.addToReserves(viper);
        execute(new DamageHumanFighterEvent(viper.id()));

        assertFalse(galacticaBoard.reserves().contains(viper));
        assertTrue(galacticaBoard.damagedShips().contains(viper));
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
    void shouldDamageAViperInSpaceOrInTheReserves() {
        val damagedViper = galacticaBoard.damagedShips().iterator().next();
        assertInvalid(new DamageHumanFighterEvent(damagedViper.id()));
    }

    @Test
    void shouldNotDamageARaptor() {
        assertInvalid(new DamageHumanFighterEvent(11));
    }

    // "Forced to Eject": Brendan "Hot Dog" Costanza's viper is destroyed, not damaged; he still goes to "Sickbay".
    @Test
    void hotDogsViperIsDestroyed() {
        player(1).character(BRENDAN_HOTDOG_COSTANZA);
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        viper.pilot(BRENDAN_HOTDOG_COSTANZA);

        execute(new DamageHumanFighterEvent(viper.id()));

        assertEquals(SICKBAY, locate(BRENDAN_HOTDOG_COSTANZA));
        assertFalse(galacticaBoard.damagedShips().contains(viper));
        assertTrue(game.removedComponents().items().contains(viper));
    }

    // An assault raptor counts as a viper and is destroyed, in the "Reserves" too (rule 11.3.2.3, F-d897ee).
    @Test
    void shouldDestroyAnAssaultRaptorInTheReserves() {
        val assaultRaptor = galacticaBoard.reserves().stream()
                .filter(AssaultRaptor.class::isInstance)
                .findFirst()
                .orElseThrow();

        execute(new DamageHumanFighterEvent(assaultRaptor.id()));

        assertFalse(galacticaBoard.reserves().contains(assaultRaptor));
        assertFalse(galacticaBoard.damagedShips().contains(assaultRaptor));
        assertTrue(game.removedComponents().contains(assaultRaptor));
    }
}
