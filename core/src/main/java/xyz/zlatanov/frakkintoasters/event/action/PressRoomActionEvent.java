package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.*;
import xyz.zlatanov.frakkintoasters.event.player.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public record PressRoomActionEvent(int playerNumber, int targetPlayer) implements ActionEvent {

    @Override
    public void apply(Game game) {
        val cardDrawn = game.decks().mutiny().draw();
        game.player(targetPlayer).mutinyCards().add(cardDrawn);
    }

    @Override
    public List<Followup> followup(Game game) {
        return followWith(
                allOf(
                        new PlayerDecisionEvent(targetPlayer, DiscardDownTo1MutinyCardEvent.class)),
                oneOf(
                        new PlayerDecisionEvent(playerNumber, Discard1MutinyCardEvent.class),
                        new NoOpEvent(playerNumber))
        );
    }
}
