package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.turn.StartGameEvent;
import xyz.zlatanov.frakkintoasters.event.turn.StartGameEventProcessor;

public class StackedStartGameEventProcessor extends EventProcessor<StackedStartGameEvent> {

    @Override
    public Followup process() {
        val setUp = new StartGameEventProcessor().prepare(game, new StartGameEvent()).run(ops);
        for (val card : event.crisisCards().reversed()) {
            ops.player(1).placeCrisisCardOnTop(card); // the first card ends up on top
        }
        return setUp;
    }
}
