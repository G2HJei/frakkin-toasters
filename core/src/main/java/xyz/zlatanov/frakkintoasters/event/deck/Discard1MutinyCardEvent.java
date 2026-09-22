package xyz.zlatanov.frakkintoasters.event.deck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

public record Discard1MutinyCardEvent(int playerNumber, MutinyCard cardToDiscard) implements PlayerEvent {

}
