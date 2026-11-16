package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RagnarAnchorageEventProcessorTest extends EventTestHarness<RagnarAnchorageEvent> {

    // "The Admiral may repair up to 3 vipers and 1 raptor. These ships may be damaged or even destroyed."
    @Test
    void upToThreeVipersAndOneRaptorAreRepaired() {
        val vipers = galacticaBoard.reserves().stream().filter(Viper.class::isInstance).limit(4).toList();
        vipers.forEach(viper -> ops.ship(viper.id()).damage());
        val raptors = galacticaBoard.reserves().stream().filter(Raptor.class::isInstance).limit(2).toList();
        raptors.forEach(raptor -> ops.ship(raptor.id()).destroy());
        val reservesBefore = galacticaBoard.reserves().size();

        execute(new RagnarAnchorageEvent());

        assertEquals(reservesBefore + 3 + 1, galacticaBoard.reserves().size());
    }

    // Mark VIIs are the ones in the damaged vipers box from the start, and they are repaired first.
    @Test
    void markVIIsAreRepairedFirst() {
        execute(new RagnarAnchorageEvent());

        assertEquals(3, galacticaBoard.reserves().stream().filter(ViperMarkVII.class::isInstance).count());
    }

    @Test
    void nothingToRepairIsFine() {
        galacticaBoard.damagedShips().forEach(viper -> ops.ship(viper.id()).repair());
        val reserves = galacticaBoard.reserves().size();

        execute(new RagnarAnchorageEvent());

        assertEquals(reserves, galacticaBoard.reserves().size());
    }
}
