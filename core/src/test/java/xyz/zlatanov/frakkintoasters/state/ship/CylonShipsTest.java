package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.STRUCTURAL_DAMAGE;

class CylonShipsTest {

    @Test
    void shouldCreateAndReturnBasestar() {
        val ships = CylonShips.builder().basestars(1).build();

        val basestar = ships.basestar();
        assertNotNull(basestar);
        assertTrue(ships.basestars().isEmpty());

        ships.returned(basestar);
        assertEquals(1, ships.basestars().size());
        assertSame(basestar, ships.basestars().getFirst());
    }

    @Test
    void shouldThrowExceptionWhenNoBasestarsLeft() {
        val ships = CylonShips.builder().basestars(0).build();
        assertThrows(AssertionError.class, ships::basestar);
    }

    @Test
    void shouldCreateAndReturnRaider() {
        val ships = CylonShips.builder().raiders(1).build();

        val raider = ships.raider();
        assertNotNull(raider);
        assertTrue(ships.raiders().isEmpty());

        ships.returned(raider);
        assertEquals(1, ships.raiders().size());
        assertSame(raider, ships.raiders().getFirst());
    }

    @Test
    void shouldThrowExceptionWhenNoRaidersLeft() {
        val ships = CylonShips.builder().raiders(0).build();
        assertThrows(AssertionError.class, ships::raider);
    }

    @Test
    void shouldCreateAndReturnHeavyRaider() {
        val ships = CylonShips.builder().heavyRaiders(1).build();

        val heavyRaider = ships.heavyRaider();
        assertNotNull(heavyRaider);
        assertTrue(ships.heavyRaiders().isEmpty());

        ships.returned(heavyRaider);
        assertEquals(1, ships.heavyRaiders().size());
        assertSame(heavyRaider, ships.heavyRaiders().getFirst());
    }

    @Test
    void shouldThrowExceptionWhenNoHeavyRaidersLeft() {
        val ships = CylonShips.builder().heavyRaiders(0).build();
        assertThrows(AssertionError.class, ships::heavyRaider);
    }

    @Test
    void shouldCreateAndReturnCenturion() {
        val ships = CylonShips.builder().centurions(1).build();

        val centurion = ships.centurion();
        assertNotNull(centurion);
        assertTrue(ships.centurions().isEmpty());

        ships.returnedCenturion(centurion);
        assertEquals(1, ships.centurions().size());
        assertSame(centurion, ships.centurions().getFirst());
    }

    @Test
    void shouldThrowExceptionWhenNoCenturionsLeft() {
        val ships = CylonShips.builder().centurions(0).build();
        assertThrows(AssertionError.class, ships::centurion);
    }

    @Test
    void shouldAssignUniqueIds() {
        val ships = CylonShips.builder().basestars(2).raiders(2).heavyRaiders(2).centurions(2).build();

        val b1 = ships.basestar();
        val b2 = ships.basestar();
        assertNotEquals(b1.id(), b2.id());

        val r1 = ships.raider();
        val r2 = ships.raider();
        assertNotEquals(r1.id(), r2.id());

        val h1 = ships.heavyRaider();
        val h2 = ships.heavyRaider();
        assertNotEquals(h1.id(), h2.id());

        val c1 = ships.centurion();
        val c2 = ships.centurion();
        assertNotEquals(c1.id(), c2.id());
    }

    @Test
    void shouldClearBasestarDamageOnReturn() {
        val ships = CylonShips.builder().basestars(1).build();
        val basestar = ships.basestar();
        basestar.damage(STRUCTURAL_DAMAGE);
        assertFalse(basestar.damage().isEmpty());

        ships.returned(basestar);
        val returned = ships.basestar();
        assertTrue(returned.damage().isEmpty());
        assertSame(basestar, returned);
    }
}
