package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateHeavyRaidersAndCenturionsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.Arrays;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_HANGAR_BAY;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_3_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_5_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAVIL;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;


class CylonFleetEventProcessorTest extends EventTestHarness<CylonFleetEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(CAVIL);
        moveTo(CYLON_FLEET, CAVIL);
    }

    @Test
    void shouldLaunch2RaidersAndHeavyRaiderFromSingleBasestar() {
        basestarAt(GALACTICA_SPACE_3_OCLOCK);
        execute(new CylonFleetEvent(1, null));
        assertCylonShips(GALACTICA_SPACE_3_OCLOCK);
    }

    @Test
    void shouldLaunch2RaidersAndHeavyRaiderFromEachBasestar() {
        basestarAt(GALACTICA_SPACE_3_OCLOCK);
        basestarAt(GALACTICA_SPACE_5_OCLOCK);

        execute(new CylonFleetEvent(1, null));

        assertCylonShips(GALACTICA_SPACE_3_OCLOCK, GALACTICA_SPACE_5_OCLOCK);
    }

    // "Disabled Hangar": the basestar launches neither raiders nor heavy raiders (rule 11.7.3).
    @Test
    void shouldNotLaunchFromABasestarWithADisabledHangar() {
        basestarAt(GALACTICA_SPACE_3_OCLOCK).damage(DISABLED_HANGAR_BAY);

        execute(new CylonFleetEvent(1, null));

        assertShipCount(GALACTICA_SPACE_3_OCLOCK, Raider.class, 0);
        assertShipCount(GALACTICA_SPACE_3_OCLOCK, HeavyRaider.class, 0);
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

    // "Negotiation": no Cylon ship launches or activates for the rest of the turn.
    // Its activations would do nothing, so the location has no action.
    @Test
    void noActionWhileTheShipsAreHeld() {
        basestarAt(GALACTICA_SPACE_3_OCLOCK);
        ops.holdCylonShips();

        assertInvalid(new CylonFleetEvent(1, null));
    }
}
