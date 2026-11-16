package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.loyalty.SleeperAgentPhaseEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class ObjectiveInstructionsEventProcessor extends EventProcessor<ObjectiveInstructionsEvent> {

    public static final int SLEEPER_AGENT_DISTANCE = 4;

    @Override
    protected boolean isValid() {
        return event.distanceBefore() >= 0 && event.distanceBefore() <= game.distanceTraveled();
    }

    @Override
    public Followup process() {
        val crossed = event.distanceBefore() < SLEEPER_AGENT_DISTANCE && game.distanceTraveled() >= SLEEPER_AGENT_DISTANCE;
        return crossed ? single(new SleeperAgentPhaseEvent()) : NONE;
    }
}
