package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.ShipMoved;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipZone.*;

// Placing Cylon ships on the main game board (rule 11.4.2): "1. Choose the appropriate types of Cylon ships from among
// the ships that are on neither the main game board nor the Cylon Fleet game board. ... 2. If, after step 1, there are
// not enough ships to fulfill the instructions, take ships of the appropriate types from Cylon space areas, starting
// with the lowest-numbered Cylon space area"
class PlaceCylonShipOpsTest extends TestHarness {

    @Test
    void shouldTakeTheShipFromTheSupply() {
        val raider = cylonShips.next(Raider.class).orElseThrow();

        ops.galactica().placeRaider(GALACTICA_SPACE_8_OCLOCK);

        assertEquals(List.of(new ShipMoved(raider.id(), CYLON_SUPPLY, GALACTICA_SPACE, GALACTICA_SPACE_8_OCLOCK)),
                journal.facts());
    }

    @Test
    void shouldTakeTheShipFromTheLowestNumberedCylonSpaceAreaWhenTheSupplyIsOut() {
        raiderAt(CYLON_FLEET_SPACE_5_6);
        val lowest = raiderAt(CYLON_FLEET_SPACE_2);
        emptyTheRaiderSupply();

        ops.galactica().placeRaider(GALACTICA_SPACE_8_OCLOCK);

        assertEquals(List.of(new ShipMoved(lowest.id(), CYLON_FLEET_SPACE, GALACTICA_SPACE, GALACTICA_SPACE_8_OCLOCK)),
                journal.facts());
        assertNoShips(CYLON_FLEET_SPACE_2);
        assertShipCount(CYLON_FLEET_SPACE_5_6, 1);
    }

    // Only the type the effect places: a basestar in a lower area doesn't count for a raider.
    @Test
    void shouldTakeOnlyAShipOfTheType() {
        basestarAt(CYLON_FLEET_SPACE_1);
        val raider = raiderAt(CYLON_FLEET_SPACE_4);
        emptyTheRaiderSupply();

        ops.galactica().placeRaider(GALACTICA_SPACE_8_OCLOCK);

        assertEquals(List.of(new ShipMoved(raider.id(), CYLON_FLEET_SPACE, GALACTICA_SPACE, GALACTICA_SPACE_8_OCLOCK)),
                journal.facts());
    }

    // "3. If, after step 2, there are still not enough ships to fulfill the instructions, ... ships are not placed."
    @Test
    void shouldPlaceNothingWhenThereIsNoShipOfTheTypeLeft() {
        emptyTheRaiderSupply();

        ops.galactica().placeRaider(GALACTICA_SPACE_8_OCLOCK);

        assertEquals(List.of(), journal.facts());
    }

    // A basestar launches its raiders the same way.
    @Test
    void shouldLaunchRaidersFromTheCylonFleetBoardWhenTheSupplyIsOut() {
        val basestar = basestarAt(GALACTICA_SPACE_10_OCLOCK);
        raiderAt(CYLON_FLEET_SPACE_3);
        raiderAt(CYLON_FLEET_SPACE_7_8);
        emptyTheRaiderSupply();

        ops.ship(basestar.id()).launchRaiders(3);

        assertShipCount(GALACTICA_SPACE_10_OCLOCK, Raider.class, 2);
        assertNoShips(CYLON_FLEET_SPACE_3);
        assertNoShips(CYLON_FLEET_SPACE_7_8);
    }

    private void emptyTheRaiderSupply() {
        while (cylonShips.raider().isPresent()) {
            // takes the raiders out of the supply, off every board
        }
    }
}
