package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;

public record PresidentsOfficeActionEvent(int playerNumber) implements ActionEvent {

    @Override
    public void apply(Game game) {
        val drawnCards = game.decks().politics().draw(2);
        player(game).skillCards().add(drawnCards);
    }
}
