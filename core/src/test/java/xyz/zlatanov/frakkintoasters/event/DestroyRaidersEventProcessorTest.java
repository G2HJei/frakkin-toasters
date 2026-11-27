package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_5_OCLOCK;

class DestroyRaidersEventProcessorTest extends EventTestHarness<DestroyRaidersEvent> {

    @Test
    void shouldDestroyTheRaiders() {
        val first = raiderAt(GALACTICA_SPACE_5_OCLOCK);
        val second = raiderAt(GALACTICA_SPACE_5_OCLOCK);
        raiderAt(GALACTICA_SPACE_5_OCLOCK);
        val supply = cylonShips.raiders().size();

        execute(new DestroyRaidersEvent(Set.of(first.id(), second.id())));

        assertShipCount(GALACTICA_SPACE_5_OCLOCK, 1);
        assertEquals(supply + 2, cylonShips.raiders().size()); // they may return later in the game (rule 11.7.1.1)
    }

    @Test
    void shouldDestroyRaidersInSpace() {
        assertInvalid(new DestroyRaidersEvent(Set.of(raider().id())));
    }

    @Test
    void shouldDestroyOnlyRaiders() {
        assertInvalid(new DestroyRaidersEvent(Set.of(basestarAt(GALACTICA_SPACE_5_OCLOCK).id())));
    }

    @Test
    void shouldNameARaider() {
        assertInvalid(new DestroyRaidersEvent(Set.of()));
    }
}
