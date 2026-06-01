package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class HangarDeckEventProcessor extends EventProcessor<HangarDeckEvent> {

    @Override
    public Followup processEvent() {
        val playerNumber = event.playerNumber();
        return all(
                new PlayerDecisionEvent<>(playerNumber, LaunchViperEvent.class),
                new PlayerDecisionEvent<>(playerNumber, ActionEvent.class));
    }

}
