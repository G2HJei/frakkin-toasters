package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.EventFollowup;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

public record HangarDeckActionEvent(int playerNumber) implements ActionEvent {

    @Override
    public void apply(Game game) {
        //do nothing
    }

    @Override
    public EventFollowup followup(Game game) {
        return EventFollowup.followup(
                new PlayerDecisionEvent(playerNumber, LaunchViperEvent.class),
                new PlayerDecisionEvent(playerNumber, ActionEvent.class));
    }

}
