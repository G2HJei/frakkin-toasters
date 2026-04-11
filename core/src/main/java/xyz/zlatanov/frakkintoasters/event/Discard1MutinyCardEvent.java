package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import java.util.List;

public record Discard1MutinyCardEvent(int playerNumber, MutinyCard cardToDiscard) implements PlayerEvent {

    @Override
    public List<Followup> apply(Game game) {
        player(game).mutinyCards()
                .remove(cardToDiscard)
                .discard(cardToDiscard);
        return List.of();
    }
}
