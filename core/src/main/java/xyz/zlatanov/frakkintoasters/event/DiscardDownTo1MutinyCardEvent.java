package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import java.util.List;

public record DiscardDownTo1MutinyCardEvent(int playerNumber, MutinyCard cardToKeep) implements PlayerEvent {

    @Override
    public List<Followup> apply(Game game) {
        val mutinyCardDeck = player(game).mutinyCards();
        val cardsToDiscard = mutinyCardDeck
                .cards()
                .stream()
                .filter(c -> c != cardToKeep)
                .toList();
        mutinyCardDeck
                .remove(cardsToDiscard)
                .discard(cardsToDiscard);
        return List.of();
    }
}
