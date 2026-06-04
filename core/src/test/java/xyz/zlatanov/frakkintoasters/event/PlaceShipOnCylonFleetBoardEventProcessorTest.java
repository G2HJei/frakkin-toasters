package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

class PlaceShipOnCylonFleetBoardEventProcessorTest extends EventTestHarness<PlaceShipOnCylonFleetBoardEvent> {

    @ParameterizedTest
    @MethodSource("shouldPlaceShipOnCorrespondingSpaceAreaArgs")
    void shouldPlaceShipOnCorrespondingSpaceArea(Class<Ship> shipClass, int nextDieRoll, Location placementLocation) {
        nextRoll(nextDieRoll);
        execute(new PlaceShipOnCylonFleetBoardEvent(ShipType.of(shipClass)));
        assertShipCount(placementLocation, shipClass, 1);
    }

    static Stream<Arguments> shouldPlaceShipOnCorrespondingSpaceAreaArgs() {
        return Stream.of(
                arguments(Basestar.class, 1, CYLON_FLEET_SPACE_1),
                arguments(Basestar.class, 2, CYLON_FLEET_SPACE_2),
                arguments(Basestar.class, 3, CYLON_FLEET_SPACE_3),
                arguments(Basestar.class, 4, CYLON_FLEET_SPACE_4),
                arguments(Basestar.class, 5, CYLON_FLEET_SPACE_5_6),
                arguments(Basestar.class, 6, CYLON_FLEET_SPACE_5_6),
                arguments(Raider.class, 7, CYLON_FLEET_SPACE_7_8),
                arguments(HeavyRaider.class, 8, CYLON_FLEET_SPACE_7_8)
        );
    }

    @Test
    void shouldTransferShipsToMainBoard() {
        raiderAt(CYLON_FLEET_SPACE_7_8);
        raiderAt(CYLON_FLEET_SPACE_7_8);
        raiderAt(CYLON_FLEET_SPACE_1);
        outOfRaiders();

        execute(new PlaceShipOnCylonFleetBoardEvent(RAIDER));

        assertNoShips(CYLON_FLEET_SPACE_7_8);
        assertShipCount(CYLON_FLEET_SPACE_1, 1);
        assertShipCount(GALACTICA_SPACE_12_OCLOCK, 2);
    }

    private void outOfRaiders() {
        cylonShips.raider().ifPresent(r -> outOfRaiders());
    }

}