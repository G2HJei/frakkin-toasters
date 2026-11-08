package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ScriptEventProcessor extends EventProcessor<ScriptEvent> {

    @Override
    public Followup process() {
        ops.startGame();
        return event.script();
    }
}
