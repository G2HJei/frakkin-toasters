package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.ActivateHeavyRaidersAndCenturionsAction;
import xyz.zlatanov.frakkintoasters.event.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.EventProcessorTestHarness;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.Arrays;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;


class CylonFleetEventProcessorTest extends EventProcessorTestHarness<CylonFleetEvent> {

    @Test
    void shouldLaunch2RaidersAndHeavyRaiderFromSingleBasestar() {
        basestarAt(GALACTICA_SPACE_2_OCLOCK);
        executeAndAssertNoFollowup(new CylonFleetEvent(1, null));
        assertCylonShips(GALACTICA_SPACE_2_OCLOCK);
    }

    @Test
    void shouldLaunch2RaidersAndHeavyRaiderFromEachBasestar() {
        basestarAt(GALACTICA_SPACE_2_OCLOCK);
        basestarAt(GALACTICA_SPACE_4_OCLOCK);
        executeAndAssertNoFollowup(new CylonFleetEvent(1, null));
        assertCylonShips(GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK);
    }

    @Test
    void shouldFollowUpWithActivateRaidersEvent() {
        executeAndAssertFollowup(new CylonFleetEvent(1, RAIDER), single(new ActivateRaidersEvent()));
    }

    @Test
    void shouldFollowUpWithActivateHeavyRaidersAndCenturionsEvent() {
        executeAndAssertFollowup(new CylonFleetEvent(1, HEAVY_RAIDER), single(new ActivateHeavyRaidersAndCenturionsAction()));
    }

    @Test
    void shouldAcceptOnlyValidOrEmptyTypeToActivate() {
        Arrays.stream(ShipType.values())
                .filter(t -> t != HEAVY_RAIDER && t != RAIDER)
                .forEach(invalidType -> assertInvalid(new CylonFleetEvent(1, invalidType)));
    }

    void assertCylonShips(Location... locations) {
        for (val location : locations) {
            assertShipCount(location, Raider.class, 2);
            assertShipCount(location, HeavyRaider.class, 1);
        }
    }
}