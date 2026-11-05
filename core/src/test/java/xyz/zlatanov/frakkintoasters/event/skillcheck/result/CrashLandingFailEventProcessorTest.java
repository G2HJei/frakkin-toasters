package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;

class CrashLandingFailEventProcessorTest extends EventTestHarness<CrashLandingFailEvent> {

    @Test
    void shouldChooseBetweenFuelAndMoraleWithSickbay() {
        game.currentPlayerNumber(2);

        execute(new CrashLandingFailEvent());

        assertFollowup(one(
                single(new LoseResourceEvent(FUEL, 1)),
                all(new LoseResourceEvent(MORALE, 1), new SendToSickbayEvent(2))));
    }
}
