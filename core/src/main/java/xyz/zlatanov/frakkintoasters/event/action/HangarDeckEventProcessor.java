package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class HangarDeckEventProcessor extends EventProcessor<HangarDeckEvent> {

    @Override
    public Followup process() {
        return all(
                new PlayerDecisionEvent<>(player.number(), LaunchViperEvent.class),
                new PlayerDecisionEvent<>(player.number(), ActionEvent.class));
    }

}
