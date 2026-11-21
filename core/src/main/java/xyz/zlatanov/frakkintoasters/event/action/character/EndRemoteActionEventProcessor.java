package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class EndRemoteActionEventProcessor extends EventProcessor<EndRemoteActionEvent> {

    @Override
    public Followup process() {
        ops.endRemoteAction();
        return NONE;
    }
}
