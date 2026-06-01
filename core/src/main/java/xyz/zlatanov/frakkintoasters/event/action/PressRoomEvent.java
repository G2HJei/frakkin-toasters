package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.*;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public record PressRoomEvent(int playerNumber, int targetPlayer) implements ActionEvent {

    @Override
    public Followup apply(Game game) {
        val cardDrawn = game.decks().mutiny().draw();
        game.player(targetPlayer).mutinyCards().addOnTop(cardDrawn);
        return all(
                single(new PlayerDecisionEvent<>(targetPlayer, DiscardDownTo1MutinyCardEvent.class)),
                one(new PlayerDecisionEvent<>(playerNumber, Discard1MutinyCardEvent.class),
                        new NoOpEvent(playerNumber))
        );
    }

}
