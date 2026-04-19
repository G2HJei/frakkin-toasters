package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;

public record ResurrectionShipActionEvent(int playerNumber) implements ActionEvent {

    @Override
    public Followup apply(Game game) {
        val drawn = game.decks().superCrisis().draw();
        player(game).superCrisisCards().add(drawn);
        return Followup.NONE;
    }
}
