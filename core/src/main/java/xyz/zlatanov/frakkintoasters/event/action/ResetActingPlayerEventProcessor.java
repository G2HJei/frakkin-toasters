package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class ResetActingPlayerEventProcessor extends EventProcessor<ResetActingPlayerEvent> {

    @Override
    public Followup process() {
        if (game.turn().actingPlayerNumber() != game.turn().currentPlayerNumber()) {
            ops.resetActingPlayer();
        }
        return NONE;
    }
}
