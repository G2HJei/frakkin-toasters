package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;

public class CrashLandingFailEventProcessor extends EventProcessor<CrashLandingFailEvent> {

    // The Admiral chooses.
    @Override
    public Followup process() {
        val currentPlayer = game.turn().currentPlayerNumber();
        return one(
                single(new LoseResourceEvent(FUEL, 1)),
                all(new LoseResourceEvent(MORALE, 1), new SendToSickbayEvent(currentPlayer)));
    }
}
