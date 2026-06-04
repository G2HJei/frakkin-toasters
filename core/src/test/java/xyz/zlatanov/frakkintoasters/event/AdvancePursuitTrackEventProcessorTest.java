package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.track.Pursuit;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.track.Pursuit.*;

class AdvancePursuitTrackEventProcessorTest extends EventTestHarness<AdvancePursuitTrackEvent> {

    AdvancePursuitTrackEvent event = new AdvancePursuitTrackEvent();

    static Stream<Arguments> pursuitTrackTestArgs() {
        val decisionEvent = new PlayerDecisionEvent<>(2, PlaceCivilianShipEvent.class);
        return Stream.of(
                arguments(START, ONE_CIVILIAN_SHIP, single(decisionEvent)),
                arguments(ONE_CIVILIAN_SHIP, POSITION_2, Followup.NONE),
                arguments(POSITION_2, TWO_CIVILIAN_SHIPS, all(decisionEvent, decisionEvent)),
                arguments(TWO_CIVILIAN_SHIPS, START, Followup.NONE)
        );
    }

    @ParameterizedTest
    @MethodSource("pursuitTrackTestArgs")
    void shouldAdvancePursuitTrackWithFollowup(Pursuit startingPursuit, Pursuit pursuitAfterEvent, Followup expectedFollowup) {
        setup(startingPursuit);

        execute(event);

        assertFollowup(expectedFollowup);
        assertEquals(pursuitAfterEvent, cylonFleetBoard.pursuitTrack());
    }

    @Test
    void shouldTransferAlLShipsToMainBoard() {
        setup(TWO_CIVILIAN_SHIPS);
        setupShipsToTransfer();

        execute(event);

        assertShipsTransfer();
    }

    private void setup(Pursuit startingPursuit) {
        while (cylonFleetBoard.pursuitTrack() != startingPursuit) {
            cylonFleetBoard.advancePursuit();
        }
        player(2).character(LEE_APOLLO_ADAMA);
        cag(LEE_APOLLO_ADAMA);
    }

    private void setupShipsToTransfer() {
        basestarAt(CYLON_FLEET_SPACE_1);
        raiderAt(CYLON_FLEET_SPACE_2);
        heavyRaiderAt(CYLON_FLEET_SPACE_3);
    }

    private void assertShipsTransfer() {
        assertNoShips(CYLON_FLEET_SPACE_1);
        assertNoShips(CYLON_FLEET_SPACE_2);
        assertNoShips(CYLON_FLEET_SPACE_3);
        assertShipCount(GALACTICA_SPACE_2_OCLOCK, Basestar.class, 1);
        assertShipCount(GALACTICA_SPACE_4_OCLOCK, Raider.class, 1);
        assertShipCount(GALACTICA_SPACE_6_OCLOCK, HeavyRaider.class, 1);
    }
}