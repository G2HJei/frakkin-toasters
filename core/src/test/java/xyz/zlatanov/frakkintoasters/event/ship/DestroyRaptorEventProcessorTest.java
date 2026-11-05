package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DestroyRaptorEventProcessorTest extends EventTestHarness<DestroyRaptorEvent> {

    @Test
    void shouldDestroyRaptorInReserves() {
        val raptors = raptorsInReserves();

        execute(new DestroyRaptorEvent());

        assertEquals(raptors.size() - 1, raptorsInReserves().size());
        assertTrue(game.removedComponents().contains(raptors.stream()
                .filter(r -> !raptorsInReserves().contains(r))
                .findFirst()
                .orElseThrow()));
    }

    @Test
    void shouldNotDestroyAssaultRaptor() {
        raptorsInReserves().forEach(galacticaBoard::removeFromReserves);
        val reserves = Set.copyOf(galacticaBoard.reserves());

        execute(new DestroyRaptorEvent());

        assertEquals(reserves, galacticaBoard.reserves());
    }

    private List<Ship> raptorsInReserves() {
        return galacticaBoard.reserves().stream()
                .filter(Raptor.class::isInstance)
                .toList();
    }
}
