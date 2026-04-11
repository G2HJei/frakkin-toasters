package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

public record PresidentsOfficeActionEvent(int playerNumber) implements ActionEvent {

    @Override
    public List<Followup> apply(Game game) {
        val drawnCards = game.decks().politics().draw(2);
        player(game).skillCards().add(drawnCards);
        return List.of();
    }
}
