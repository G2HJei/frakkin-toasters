package xyz.zlatanov.frakkintoasters.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.ship.ShipType.*;

class ShipsHolderTest {

    @Test
    void shouldCreateAndRemoveBasestar() {
        val ships = ShipsHolder.builder().basestars(1).build();

        val basestar = ships.basestar();
        assertNotNull(basestar);
        assertEquals(0, ships.basestars());

        ships.removed(BASESTAR);
        assertEquals(1, ships.basestars());
    }

    @Test
    void shouldThrowExceptionWhenNoBasestarsLeft() {
        val ships = ShipsHolder.builder().basestars(0).build();
        assertThrows(FrakCallTheAdmiralException.class, ships::basestar);
    }

    @Test
    void shouldThrowExceptionWhenRemovingBasestarBeyondLimit() {
        val ships = ShipsHolder.builder().basestars(1).build();
        assertThrows(FrakCallTheAdmiralException.class, () -> ships.removed(BASESTAR));
    }

    @Test
    void shouldCreateAndRemoveRaider() {
        val ships = ShipsHolder.builder().raiders(1).build();

        val raider = ships.raider();
        assertNotNull(raider);
        assertEquals(0, ships.raiders());

        ships.removed(RAIDER);
        assertEquals(1, ships.raiders());
    }

    @Test
    void shouldThrowExceptionWhenNoRaidersLeft() {
        val ships = ShipsHolder.builder().raiders(0).build();
        assertThrows(FrakCallTheAdmiralException.class, ships::raider);
    }

    @Test
    void shouldThrowExceptionWhenRemovingRaiderBeyondLimit() {
        val ships = ShipsHolder.builder().raiders(1).build();
        assertThrows(FrakCallTheAdmiralException.class, () -> ships.removed(RAIDER));
    }

    @Test
    void shouldCreateAndRemoveHeavyRaider() {
        val ships = ShipsHolder.builder().heavyRaiders(1).build();

        val heavyRaider = ships.heavyRaider();
        assertNotNull(heavyRaider);
        assertEquals(0, ships.heavyRaiders());

        ships.removed(HEAVY_RAIDER);
        assertEquals(1, ships.heavyRaiders());
    }

    @Test
    void shouldThrowExceptionWhenNoHeavyRaidersLeft() {
        val ships = ShipsHolder.builder().heavyRaiders(0).build();
        assertThrows(FrakCallTheAdmiralException.class, ships::heavyRaider);
    }

    @Test
    void shouldThrowExceptionWhenRemovingHeavyRaiderBeyondLimit() {
        val ships = ShipsHolder.builder().heavyRaiders(1).build();
        assertThrows(FrakCallTheAdmiralException.class, () -> ships.removed(HEAVY_RAIDER));
    }

    @Test
    void shouldCreateAndRemoveCenturion() {
        val ships = ShipsHolder.builder().centurions(1).build();

        val centurion = ships.centurion();
        assertNotNull(centurion);
        assertEquals(0, ships.centurions());

        ships.removedCenturion();
        assertEquals(1, ships.centurions());
    }

    @Test
    void shouldThrowExceptionWhenNoCenturionsLeft() {
        val ships = ShipsHolder.builder().centurions(0).build();
        assertThrows(FrakCallTheAdmiralException.class, ships::centurion);
    }

    @Test
    void shouldThrowExceptionWhenRemovingCenturionBeyondLimit() {
        val ships = ShipsHolder.builder().centurions(1).build();
        assertThrows(FrakCallTheAdmiralException.class, ships::removedCenturion);
    }
}
