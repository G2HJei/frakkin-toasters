package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

public record Discard1MutinyCardEvent(int playerNumber, MutinyCard cardToDiscard) implements PlayerEvent {

    @Override
    public Followup apply(Game game) {
        player(game).mutinyCards()
                .remove(cardToDiscard)
                .discard(cardToDiscard);
        return Followup.NONE;
    }
}
