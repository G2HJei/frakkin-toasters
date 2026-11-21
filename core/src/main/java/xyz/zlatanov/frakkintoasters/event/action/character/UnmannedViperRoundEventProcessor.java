package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.ActivateUnmannedViperEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.canActivateUnmanned;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class UnmannedViperRoundEventProcessor extends EventProcessor<UnmannedViperRoundEvent> {

    @Override
    protected boolean isValid() {
        return event.activationsLeft() > 0;
    }

    // "Up to": he may stop before each activation.
    @Override
    public Followup process() {
        if (!canActivateUnmanned(game)) {
            return NONE;
        }
        val next = event.activationsLeft() > 1
                ? single(new UnmannedViperRoundEvent(event.playerNumber(), event.activationsLeft() - 1))
                : NONE;
        return one(all(single(new PlayerDecisionEvent<>(event.playerNumber(), ActivateUnmannedViperEvent.class)), next),
                single(new NoOpEvent(event.playerNumber())));
    }
}
