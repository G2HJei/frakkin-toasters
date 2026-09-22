package xyz.zlatanov.frakkintoasters.event.deck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

public record DiscardDownTo1MutinyCardEvent(int playerNumber, MutinyCard cardToKeep) implements PlayerEvent {

}
