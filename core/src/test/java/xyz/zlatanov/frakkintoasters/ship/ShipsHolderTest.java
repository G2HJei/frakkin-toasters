package xyz.zlatanov.frakkintoasters.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;

import static org.junit.jupiter.api.Assertions.*;

class ShipsHolderTest {

    @Test
    void shouldCreateAndRemoveBasestar() {
        val holder = ShipsHolder.builder().basestars(1).build();

        val basestar = holder.basestar();
        assertNotNull(basestar);
        assertEquals(0, holder.basestars());
        
        holder.removed(ShipType.BASESTAR);
        assertEquals(1, holder.basestars());
    }

    @Test
    void shouldThrowExceptionWhenNoBasestarsLeft() {
        val holder = ShipsHolder.builder().basestars(0).build();
        assertThrows(FrakCallTheAdmiralException.class, holder::basestar);
    }

    @Test
    void shouldThrowExceptionWhenRemovingBasestarBeyondLimit() {
        val holder = ShipsHolder.builder().basestars(1).build();
        assertThrows(FrakCallTheAdmiralException.class, () -> holder.removed(ShipType.BASESTAR));
    }

    @Test
    void shouldCreateAndRemoveRaider() {
        val holder = ShipsHolder.builder().raiders(1).build();

        val raider = holder.raider();
        assertNotNull(raider);
        assertEquals(0, holder.raiders());

        holder.removed(ShipType.RAIDER);
        assertEquals(1, holder.raiders());
    }

    @Test
    void shouldThrowExceptionWhenNoRaidersLeft() {
        val holder = ShipsHolder.builder().raiders(0).build();
        assertThrows(FrakCallTheAdmiralException.class, holder::raider);
    }

    @Test
    void shouldThrowExceptionWhenRemovingRaiderBeyondLimit() {
        val holder = ShipsHolder.builder().raiders(1).build();
        assertThrows(FrakCallTheAdmiralException.class, () -> holder.removed(ShipType.RAIDER));
    }

    @Test
    void shouldCreateAndRemoveHeavyRaider() {
        val holder = ShipsHolder.builder().heavyRaiders(1).build();

        val heavyRaider = holder.heavyRaider();
        assertNotNull(heavyRaider);
        assertEquals(0, holder.heavyRaiders());

        holder.removed(ShipType.HEAVY_RAIDER);
        assertEquals(1, holder.heavyRaiders());
    }

    @Test
    void shouldThrowExceptionWhenNoHeavyRaidersLeft() {
        val holder = ShipsHolder.builder().heavyRaiders(0).build();
        assertThrows(FrakCallTheAdmiralException.class, holder::heavyRaider);
    }

    @Test
    void shouldThrowExceptionWhenRemovingHeavyRaiderBeyondLimit() {
        val holder = ShipsHolder.builder().heavyRaiders(1).build();
        assertThrows(FrakCallTheAdmiralException.class, () -> holder.removed(ShipType.HEAVY_RAIDER));
    }

    @Test
    void shouldCreateAndRemoveCenturion() {
        val holder = ShipsHolder.builder().centurions(1).build();

        val centurion = holder.centurion();
        assertNotNull(centurion);
        assertEquals(0, holder.centurions());

        holder.removedCenturion();
        assertEquals(1, holder.centurions());
    }

    @Test
    void shouldThrowExceptionWhenNoCenturionsLeft() {
        val holder = ShipsHolder.builder().centurions(0).build();
        assertThrows(FrakCallTheAdmiralException.class, holder::centurion);
    }

    @Test
    void shouldThrowExceptionWhenRemovingCenturionBeyondLimit() {
        val holder = ShipsHolder.builder().centurions(1).build();
        assertThrows(FrakCallTheAdmiralException.class, holder::removedCenturion);
    }
}
