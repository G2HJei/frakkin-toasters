package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

public record HangarDeckActionEvent(int playerNumber) implements ActionEvent {

    @Override
    public void apply(Game game) {
        //do nothing
    }

    @Override
    public List<Event> followup(Game game) {
        return List.of(
                new PlayerDecisionEvent(playerNumber, LaunchViperEvent.class),
                new PlayerDecisionEvent(playerNumber, ActionEvent.class));
    }

}
