package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.*;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public record PressRoomActionEvent(int playerNumber, int targetPlayer) implements ActionEvent {

    @Override
    public List<Followup> apply(Game game) {
        val cardDrawn = game.decks().mutiny().draw();
        game.player(targetPlayer).mutinyCards().add(cardDrawn);
        return followWith(
                all(new PlayerDecisionEvent<>(targetPlayer, DiscardDownTo1MutinyCardEvent.class)),
                one(new PlayerDecisionEvent<>(playerNumber, Discard1MutinyCardEvent.class),
                        new NoOpEvent(playerNumber))
        );
    }

}
