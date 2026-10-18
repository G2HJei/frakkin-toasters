package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.AttackBasestarEvent;
import xyz.zlatanov.frakkintoasters.event.MoveShipEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.AttackBasestarEvent.Attacker.VIPER;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.ASSAULT_RAPTOR;

class ProtectTheFleetEventProcessorTest extends EventTestHarness<ProtectTheFleetEvent> {

    @Test
    void shouldDoNothing() {
        execute(new ProtectTheFleetEvent(1, null, null, null, null, null, null));
    }

    @Test
    void shouldLaunchFromReserves() {
        val assRaptor = new AssaultRaptor(-1);
        galacticaBoard.addToReserves(assRaptor);

        execute(ProtectTheFleetEvent.launch(1, ASSAULT_RAPTOR, GALACTICA_SPACE_6_OCLOCK));

        assertFollowup(one(new LaunchViperEvent(ASSAULT_RAPTOR, GALACTICA_SPACE_6_OCLOCK)));
    }

    @Test
    void shouldMoveHumanFighter() {
        val viper = viperMarkVIIAt(GALACTICA_SPACE_4_OCLOCK);
        execute(ProtectTheFleetEvent.move(1, viper.id(), GALACTICA_SPACE_2_OCLOCK));
        assertFollowup(one(new MoveShipEvent(viper.id(), GALACTICA_SPACE_2_OCLOCK)));
    }

    @Test
    void shouldAttackBasestar() {
        val viper = viperAt(GALACTICA_SPACE_8_OCLOCK);
        val bs = basestarAt(GALACTICA_SPACE_10_OCLOCK);

        execute(ProtectTheFleetEvent.attack(1, viper.id(), bs.id()));

        assertFollowup(one(new AttackBasestarEvent(VIPER, bs.id())));
    }

    @Test
    void shouldEscortCivilianShip() {
        viperAt(GALACTICA_SPACE_2_OCLOCK);
        val civ = civilianShipAt(GALACTICA_SPACE_2_OCLOCK);

        execute(ProtectTheFleetEvent.escort(1, civ.id()));

        assertTrue(galacticaBoard.shipsIn(GALACTICA_SPACE_2_OCLOCK, CivilianShip.class).isEmpty());
    }
}