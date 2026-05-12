package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.STRUCTURAL_DAMAGE;

class CylonShipsTest {

    @Test
    void shouldCreateAndReturnBasestar() {
        val ships = CylonShips.builder().basestars(1).build();

        val basestar = ships.basestar().orElseThrow();
        assertNotNull(basestar);
        assertTrue(ships.basestars().isEmpty());

        ships.returned(basestar);
        assertEquals(1, ships.basestars().size());
        assertSame(basestar, ships.basestars().getFirst());
    }

    @Test
    void shouldReturnEmptyWhenNoBasestarsLeft() {
        assertTrue(CylonShips.builder()
                .basestars(0).build()
                .basestar()
                .isEmpty());
    }

    @Test
    void shouldCreateAndReturnRaider() {
        val ships = CylonShips.builder().raiders(1).build();

        val raider = ships.raider().orElseThrow();
        assertNotNull(raider);
        assertTrue(ships.raiders().isEmpty());

        ships.returned(raider);
        assertEquals(1, ships.raiders().size());
        assertSame(raider, ships.raiders().getFirst());
    }

    @Test
    void shouldReturnEmptyWhenNoRaidersLeft() {
        assertTrue(CylonShips.builder()
                .raiders(0).build()
                .raider()
                .isEmpty());
    }

    @Test
    void shouldCreateAndReturnHeavyRaider() {
        val ships = CylonShips.builder().heavyRaiders(1).build();

        val heavyRaider = ships.heavyRaider().orElseThrow();
        assertNotNull(heavyRaider);
        assertTrue(ships.heavyRaiders().isEmpty());

        ships.returned(heavyRaider);
        assertEquals(1, ships.heavyRaiders().size());
        assertSame(heavyRaider, ships.heavyRaiders().getFirst());
    }

    @Test
    void shouldReturnEmptyWhenNoHeavyRaidersLeft() {
        assertTrue(CylonShips.builder()
                .heavyRaiders(0).build()
                .heavyRaider()
                .isEmpty());
    }

    @Test
    void shouldCreateAndReturnCenturion() {
        val ships = CylonShips.builder().centurions(1).build();

        val centurion = ships.centurion().orElseThrow();
        assertNotNull(centurion);
        assertTrue(ships.centurions().isEmpty());

        ships.returnedCenturion(centurion);
        assertEquals(1, ships.centurions().size());
        assertSame(centurion, ships.centurions().getFirst());
    }

    @Test
    void shouldReturnEmptyWhenNoCenturionsLeft() {
        assertTrue(CylonShips.builder()
                .centurions(0).build()
                .centurion()
                .isEmpty());
    }

    @Test
    void shouldAssignUniqueIds() {
        val ships = CylonShips.builder().basestars(2).raiders(2).heavyRaiders(2).centurions(2).build();

        val b1 = ships.basestar().orElseThrow();
        val b2 = ships.basestar().orElseThrow();
        assertNotEquals(b1.id(), b2.id());

        val r1 = ships.raider().orElseThrow();
        val r2 = ships.raider().orElseThrow();
        assertNotEquals(r1.id(), r2.id());

        val h1 = ships.heavyRaider().orElseThrow();
        val h2 = ships.heavyRaider().orElseThrow();
        assertNotEquals(h1.id(), h2.id());

        val c1 = ships.centurion().orElseThrow();
        val c2 = ships.centurion().orElseThrow();
        assertNotEquals(c1.id(), c2.id());
    }

    @Test
    void shouldClearBasestarDamageOnReturn() {
        val ships = CylonShips.builder().basestars(1).build();
        val basestar = ships.basestar().orElseThrow();
        basestar.damage(STRUCTURAL_DAMAGE);
        assertFalse(basestar.damage().isEmpty());

        ships.returned(basestar);
        val returned = ships.basestar().orElseThrow();
        assertTrue(returned.damage().isEmpty());
        assertSame(basestar, returned);
    }
}
