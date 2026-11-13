package xyz.zlatanov.frakkintoasters.event.location.cylon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAVIL;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.*;

class BasestarBridgeShipsEventProcessorTest extends EventTestHarness<BasestarBridgeShipsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(CAVIL);
    }

    // "Place 1 Basestar or 3 Raiders in any 1 area on this game board."
    @Test
    void shouldPlace3Raiders() {
        execute(new BasestarBridgeShipsEvent(1, RAIDER, CYLON_FLEET_SPACE_4));
        assertEquals(3, cylonFleetBoard.shipsIn(CYLON_FLEET_SPACE_4, Raider.class).size());
    }

    @Test
    void shouldPlace1Basestar() {
        execute(new BasestarBridgeShipsEvent(1, BASESTAR, CYLON_FLEET_SPACE_7_8));
        assertEquals(1, cylonFleetBoard.shipsIn(CYLON_FLEET_SPACE_7_8, Basestar.class).size());
    }

    // "If ... both basestars are already on either the main game board or the Cylon Fleet game board, nothing happens."
    @Test
    void shouldPlaceNoBasestarWhenNoneIsLeft() {
        while (cylonShips.available(BASESTAR) > 0) {
            ops.cylonFleet().place(BASESTAR, CYLON_FLEET_SPACE_1);
        }

        execute(new BasestarBridgeShipsEvent(1, BASESTAR, CYLON_FLEET_SPACE_7_8));

        assertEquals(0, cylonFleetBoard.shipsIn(CYLON_FLEET_SPACE_7_8, Basestar.class).size());
    }

    @Test
    void shouldPlaceOnTheCylonFleetBoardOnly() {
        assertInvalid(new BasestarBridgeShipsEvent(1, RAIDER, GALACTICA_SPACE_2_OCLOCK));
    }

    @Test
    void shouldPlaceBasestarsOrRaidersOnly() {
        assertInvalid(new BasestarBridgeShipsEvent(1, HEAVY_RAIDER, CYLON_FLEET_SPACE_4));
    }
}
