package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.*;

class CylonShipsTest {

    @Test
    void shouldCreateAndRemoveBasestar() {
        val ships = CylonShips.builder().basestars(1).build();

        val basestar = ships.basestar();
        assertNotNull(basestar);
        assertEquals(0, ships.basestars());

        ships.removed(BASESTAR);
        assertEquals(1, ships.basestars());
    }

    @Test
    void shouldThrowExceptionWhenNoBasestarsLeft() {
        val ships = CylonShips.builder().basestars(0).build();
        assertThrows(AssertionError.class, ships::basestar);
    }

    @Test
    void shouldThrowExceptionWhenRemovingBasestarBeyondLimit() {
        val ships = CylonShips.builder().build();
        assertThrows(AssertionError.class, () -> ships.removed(BASESTAR));
    }

    @Test
    void shouldCreateAndRemoveRaider() {
        val ships = CylonShips.builder().raiders(1).build();

        val raider = ships.raider();
        assertNotNull(raider);
        assertEquals(0, ships.raiders());

        ships.removed(RAIDER);
        assertEquals(1, ships.raiders());
    }

    @Test
    void shouldThrowExceptionWhenNoRaidersLeft() {
        val ships = CylonShips.builder().raiders(0).build();
        assertThrows(AssertionError.class, ships::raider);
    }

    @Test
    void shouldThrowExceptionWhenRemovingRaiderBeyondLimit() {
        val ships = CylonShips.builder().build();
        assertThrows(AssertionError.class, () -> ships.removed(RAIDER));
    }

    @Test
    void shouldCreateAndRemoveHeavyRaider() {
        val ships = CylonShips.builder().heavyRaiders(1).build();

        val heavyRaider = ships.heavyRaider();
        assertNotNull(heavyRaider);
        assertEquals(0, ships.heavyRaiders());

        ships.removed(HEAVY_RAIDER);
        assertEquals(1, ships.heavyRaiders());
    }

    @Test
    void shouldThrowExceptionWhenNoHeavyRaidersLeft() {
        val ships = CylonShips.builder().heavyRaiders(0).build();
        assertThrows(AssertionError.class, ships::heavyRaider);
    }

    @Test
    void shouldThrowExceptionWhenRemovingHeavyRaiderBeyondLimit() {
        val ships = CylonShips.builder().build();
        assertThrows(AssertionError.class, () -> ships.removed(HEAVY_RAIDER));
    }

    @Test
    void shouldCreateAndRemoveCenturion() {
        val ships = CylonShips.builder().centurions(1).build();

        val centurion = ships.centurion();
        assertNotNull(centurion);
        assertEquals(0, ships.centurions());

        ships.removedCenturion();
        assertEquals(1, ships.centurions());
    }

    @Test
    void shouldThrowExceptionWhenNoCenturionsLeft() {
        val ships = CylonShips.builder().centurions(0).build();
        assertThrows(AssertionError.class, ships::centurion);
    }

    @Test
    void shouldThrowExceptionWhenRemovingCenturionBeyondLimit() {
        val ships = CylonShips.builder().build();
        assertThrows(AssertionError.class, ships::removedCenturion);
    }
}
