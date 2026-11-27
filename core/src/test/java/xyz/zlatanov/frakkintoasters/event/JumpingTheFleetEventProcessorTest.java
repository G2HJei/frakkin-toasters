package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.endgame.EndGameEvent;
import xyz.zlatanov.frakkintoasters.event.jump.DrawDestinationCardsEvent;
import xyz.zlatanov.frakkintoasters.event.jump.MoveCylonShipsToFleetBoardEvent;
import xyz.zlatanov.frakkintoasters.event.jump.ReturnAssaultRaptorEvent;
import xyz.zlatanov.frakkintoasters.event.ship.ReturnVipersToReservesEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_7_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;

class JumpingTheFleetEventProcessorTest extends EventTestHarness<JumpingTheFleetEvent> {

    // Rule 12.1.1: move the vipers, move the Cylon ships, then the Admiral chooses the destination.
    @Test
    void theFleetJumpsStepByStep() {
        execute(new JumpingTheFleetEvent());

        assertFollowup(all(
                single(new ReturnVipersToReservesEvent(true)),
                single(new MoveCylonShipsToFleetBoardEvent()),
                single(new DrawDestinationCardsEvent())));
    }

    // "Any player whose character is piloting an assault raptor may however choose to remain in his space area and the
    // current player may choose to keep any unmanned assault raptor in its space area."
    @Test
    void thePlayersOfAssaultRaptorsChooseToRemain() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(LEE_APOLLO_ADAMA);
        val piloted = assaultRaptorAt(GALACTICA_SPACE_7_OCLOCK);
        piloted.pilot(LEE_APOLLO_ADAMA);

        execute(new JumpingTheFleetEvent());

        assertFollowup(all(
                one(new ReturnAssaultRaptorEvent(2, piloted.id()), new NoOpEvent(2)),
                single(new ReturnVipersToReservesEvent(true)),
                single(new MoveCylonShipsToFleetBoardEvent()),
                single(new DrawDestinationCardsEvent())));
    }

    // "The next time the humans jump, they do not draw a Destination Card; instead, the End Game determines the winnerPlayerNumbers."
    @Test
    void theJumpAfterEightDistanceEndsTheGame() {
        ops.galactica().reachDestination(ASTEROID_FIELD);
        ops.galactica().reachDestination(DESOLATE_MOON);
        ops.galactica().reachDestination(BARREN_PLANET);

        execute(new JumpingTheFleetEvent());

        assertFollowup(new EndGameEvent());
    }

    @Test
    void theJumpAfterSevenDistanceStillChoosesADestination() {
        ops.galactica().reachDestination(ASTEROID_FIELD);
        ops.galactica().reachDestination(DESOLATE_MOON);
        ops.galactica().reachDestination(ICY_MOON);

        execute(new JumpingTheFleetEvent());

        assertFollowup(all(
                single(new ReturnVipersToReservesEvent(true)),
                single(new MoveCylonShipsToFleetBoardEvent()),
                single(new DrawDestinationCardsEvent())));
    }
}
