package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_3_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_7_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.BRENDAN_HOTDOG_COSTANZA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class EscortEventProcessorTest extends EventTestHarness<EscortEvent> {

    private CivilianShip civilianShip;

    @BeforeEach
    void setUp() {
        player(1).character(BRENDAN_HOTDOG_COSTANZA);
        player(2).character(LAURA_ROSLIN);
        civilianShip = civilianShipAt(GALACTICA_SPACE_3_OCLOCK);
    }

    // The undamaged vipers: one of each kind from the "Reserves", and every one in space.
    @Test
    void hotDogIsAskedBeforeTheShipIsDestroyed() {
        val inSpace = viperAt(GALACTICA_SPACE_7_OCLOCK);
        val vipers = EscortEventProcessor.vipers(game);
        assertTrue(vipers.contains(inSpace.id()));

        val options = new ArrayList<Event>();
        vipers.forEach(viperId -> options.add(new EscortEvent(1, civilianShip.id(), viperId)));
        options.add(new DestroyCivilianShipEvent(civilianShip.id(), true));
        assertEquals(one(options.toArray(Event[]::new)), EscortEventProcessor.offer(game, civilianShip.id()));
        assertInvalid(new EscortEvent(1, civilianShip.id(), civilianShip.id())); // a viper, not the ship
    }

    @Test
    void aViperIsDestroyedInstead() {
        val viper = viperAt(GALACTICA_SPACE_7_OCLOCK);

        execute(new EscortEvent(1, civilianShip.id(), viper.id()));

        assertTrue(game.removedComponents().contains(viper));
        assertFalse(galacticaBoard.shipsInSpace(CivilianShip.class).stream().anyMatch(s -> s.id() == civilianShip.id()));
        assertTrue(civilianShips.cards().stream().anyMatch(s -> s.id() == civilianShip.id()));
        assertEquals(0, player(1).miracleTokens());
    }

    @Test
    void notWithoutTheMiracleToken() {
        val viper = viperAt(GALACTICA_SPACE_7_OCLOCK);
        ops.player(1).loseMiracleTokens();

        assertEquals(NONE, EscortEventProcessor.offer(game, civilianShip.id()));
        assertInvalid(new EscortEvent(1, civilianShip.id(), viper.id()));
    }

    @Test
    void notADamagedViper() {
        val damaged = galacticaBoard.damagedShips().stream().filter(Viper.class::isInstance).findFirst().orElseThrow();

        assertInvalid(new EscortEvent(1, civilianShip.id(), damaged.id()));
    }

    @Test
    void notAShipAlreadyGone() {
        val viper = viperAt(GALACTICA_SPACE_7_OCLOCK);

        assertInvalid(new EscortEvent(1, civilianShip().id(), viper.id()));
    }
}
