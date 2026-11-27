package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_3_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_5_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_7_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;

class ActivateUnmannedViperEventProcessorTest extends EventTestHarness<ActivateUnmannedViperEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
    }

    @Test
    void launchesOneFromTheReserves() {
        execute(new ActivateUnmannedViperEvent(1, ViperActivation.launch(VIPER, GALACTICA_SPACE_7_OCLOCK)));

        assertFollowup(new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_7_OCLOCK));
    }

    // Even a pilot activates an unmanned viper, not his own.
    @Test
    void movesAnUnmannedOne() {
        val unmanned = viperAt(GALACTICA_SPACE_3_OCLOCK);
        val own = viperAt(GALACTICA_SPACE_3_OCLOCK);
        ops.player(1).pilot(own.id());

        execute(new ActivateUnmannedViperEvent(1, ViperActivation.move(unmanned.id(), GALACTICA_SPACE_5_OCLOCK)));

        assertShipCount(GALACTICA_SPACE_5_OCLOCK, 1);
    }

    @Test
    void notAPilotedOne() {
        val own = viperAt(GALACTICA_SPACE_3_OCLOCK);
        ops.player(1).pilot(own.id());

        assertInvalid(new ActivateUnmannedViperEvent(1, ViperActivation.move(own.id(), GALACTICA_SPACE_5_OCLOCK)));
    }
}
