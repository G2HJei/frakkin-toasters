package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.event.ActivateHeavyRaidersAndCenturionsEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.Arrays;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;


class CylonFleetEventProcessorTest extends EventTestHarness<CylonFleetEvent> {

    @Test
    void shouldLaunch2RaidersAndHeavyRaiderFromSingleBasestar() {
        basestarAt(GALACTICA_SPACE_2_OCLOCK);
        execute(new CylonFleetEvent(1, null));
        assertCylonShips(GALACTICA_SPACE_2_OCLOCK);
    }

    @Test
    void shouldLaunch2RaidersAndHeavyRaiderFromEachBasestar() {
        basestarAt(GALACTICA_SPACE_2_OCLOCK);
        basestarAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new CylonFleetEvent(1, null));

        assertCylonShips(GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK);
    }

    @Test
    void shouldFollowUpWithActivateRaidersEvent() {
        execute(new CylonFleetEvent(1, RAIDER));
        assertFollowup(new ActivateRaidersEvent());
    }

    @Test
    void shouldFollowUpWithActivateHeavyRaidersAndCenturionsEvent() {
        execute(new CylonFleetEvent(1, HEAVY_RAIDER));
        assertFollowup(new ActivateHeavyRaidersAndCenturionsEvent());
    }

    public static Stream<Arguments> invalidShipTypes() {
        return Arrays.stream(ShipType.values())
                .filter(t -> t != HEAVY_RAIDER && t != RAIDER)
                .map(Arguments::arguments);
    }

    @ParameterizedTest
    @MethodSource("invalidShipTypes")
    void shouldAcceptOnlyValidOrEmptyTypeToActivate(ShipType invalidType) {
        assertInvalid(new CylonFleetEvent(1, invalidType));
    }

    void assertCylonShips(Location... locations) {
        for (val location : locations) {
            assertShipCount(location, Raider.class, 2);
            assertShipCount(location, HeavyRaider.class, 1);
        }
    }
}