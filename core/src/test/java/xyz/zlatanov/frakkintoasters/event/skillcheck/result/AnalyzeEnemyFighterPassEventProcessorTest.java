package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// "Repair 1 destroyed raptor."
class AnalyzeEnemyFighterPassEventProcessorTest extends EventTestHarness<AnalyzeEnemyFighterPassEvent> {

    @Test
    void shouldRepairADestroyedRaptor() {
        destroyARaptor();
        assertEquals(3, raptorsInReserves());

        execute(new AnalyzeEnemyFighterPassEvent());

        assertEquals(4, raptorsInReserves());
        assertFalse(game.removedComponents().items().stream().anyMatch(Raptor.class::isInstance));
    }

    @Test
    void shouldRepairOnlyOne() {
        destroyARaptor();
        destroyARaptor();

        execute(new AnalyzeEnemyFighterPassEvent());

        assertEquals(3, raptorsInReserves());
    }

    @Test
    void shouldDoNothingWhenNoRaptorWasDestroyed() {
        execute(new AnalyzeEnemyFighterPassEvent());

        assertEquals(4, raptorsInReserves());
    }

    private void destroyARaptor() {
        val raptor = galacticaBoard.reserves().stream().filter(Raptor.class::isInstance).findFirst().orElseThrow();
        ops.ship(raptor.id()).destroy();
    }

    private long raptorsInReserves() {
        return galacticaBoard.reserves().stream().filter(Raptor.class::isInstance).count();
    }
}
