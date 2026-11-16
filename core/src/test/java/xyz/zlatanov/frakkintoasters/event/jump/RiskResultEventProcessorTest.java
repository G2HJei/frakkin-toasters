package xyz.zlatanov.frakkintoasters.event.jump;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;
import xyz.zlatanov.frakkintoasters.event.jump.RiskShipsEvent.Risk;
import xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;

import static xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent.Area.RESERVES;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;

class RiskResultEventProcessorTest extends EventTestHarness<RiskResultEvent> {

    // "The Admiral may risk 1 raptor to roll a die. If 3 or higher, gain 1 food. Otherwise, destroy 1 raptor."
    @Test
    void aRollOfThreeGainsFood() {
        rolled(3);

        execute(new RiskResultEvent(Risk.ICY_MOON));

        assertFollowup(new GainResourceEvent(FOOD, 1));
    }

    @Test
    void aLowerRollDestroysTheRaptor() {
        rolled(2);

        execute(new RiskResultEvent(Risk.ICY_MOON));

        assertFollowup(new DestroyRaptorEvent());
    }

    // "The Admiral may risk 2 vipers to roll a die. If 6 or higher, gain 2 fuel. Otherwise, damage 2 vipers."
    @Test
    void theRefineryNeedsASix() {
        rolled(6);

        execute(new RiskResultEvent(Risk.CYLON_REFINERY));

        assertFollowup(new GainResourceEvent(FUEL, 2));
    }

    @Test
    void theRefineryDamagesTheVipersOtherwise() {
        rolled(5);

        execute(new RiskResultEvent(Risk.CYLON_REFINERY));

        assertFollowup(new DamageVipersEvent(2, RESERVES));
    }
}
