package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class DrawMotiveCardsEventProcessor extends EventProcessor<DrawMotiveCardsEvent> {

    @Override
    public Followup process() {
        ops.player(player.number()).drawMotiveCards();
        return NONE;
    }
}
