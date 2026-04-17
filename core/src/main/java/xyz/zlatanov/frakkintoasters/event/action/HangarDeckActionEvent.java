package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;

public record HangarDeckActionEvent(int playerNumber) implements ActionEvent {

    @Override
    public List<Followup> apply(Game game) {
        return followWith(all(
                new PlayerDecisionEvent<>(playerNumber, LaunchViperEvent.class),
                new PlayerDecisionEvent<>(playerNumber, ActionEvent.class)));
    }

}
