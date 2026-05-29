package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.*;

class PlaceShipOnCylonFleetBoardEventTest extends EventTest {

    @ParameterizedTest
    @MethodSource("shouldPlaceShipOnCorrespondingSpaceAreaArgs")
    void shouldPlaceShipOnCorrespondingSpaceArea(Class<Ship> shipClass, int nextDieRoll, Location placementLocation) {
        die.nextRoll(nextDieRoll);
        executeEvent(shipTypeFor(shipClass));
        assertEquals(1, cylonFleetBoard.shipsIn(placementLocation, shipClass).size());
    }

    // todo uncomment after cylonShips refactor to return Optional @Test
    void shouldTransferShipsToMainBoard() {
        val basestarToMove = basestar();
        game.boards().cylonFleet().place(CYLON_FLEET_SPACE_7_8, basestarToMove);
        game.boards().cylonFleet().place(CYLON_FLEET_SPACE_1, basestar());

        val followup = executeEvent(BASESTAR);

        assertEquals(single(new MoveCylonShipsToMainBoard(List.of(basestarToMove.id()))), followup);
    }


    static Stream<Arguments> shouldPlaceShipOnCorrespondingSpaceAreaArgs() {
        return Stream.of(
                Arguments.arguments(Basestar.class, 1, CYLON_FLEET_SPACE_1),
                Arguments.arguments(Basestar.class, 2, CYLON_FLEET_SPACE_2),
                Arguments.arguments(Basestar.class, 3, CYLON_FLEET_SPACE_3),
                Arguments.arguments(Basestar.class, 4, CYLON_FLEET_SPACE_4),
                Arguments.arguments(Basestar.class, 5, CYLON_FLEET_SPACE_5_6),
                Arguments.arguments(Basestar.class, 6, CYLON_FLEET_SPACE_5_6),
                Arguments.arguments(Raider.class, 7, CYLON_FLEET_SPACE_7_8),
                Arguments.arguments(HeavyRaider.class, 8, CYLON_FLEET_SPACE_7_8)
        );
    }

    <T extends Ship> ShipType shipTypeFor(Class<T> shipClass) {
        if (shipClass.equals(Raider.class)) {
            return RAIDER;
        } else if (shipClass.equals(HeavyRaider.class)) {
            return HEAVY_RAIDER;
        } else if (shipClass.equals(Basestar.class)) {
            return BASESTAR;
        }
        throw new FrakCallTheAdmiralException();
    }

    Followup executeEvent(ShipType shipType) {
        return execute(new PlaceShipOnCylonFleetBoardEvent(shipType));
    }
}