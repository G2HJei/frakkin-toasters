package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public record HangarDeckEvent(int playerNumber) implements ActionEvent {

    @Override
    public Followup apply(Game game) {
        return all(
                new PlayerDecisionEvent<>(playerNumber, LaunchViperEvent.class),
                new PlayerDecisionEvent<>(playerNumber, ActionEvent.class));
    }

}
