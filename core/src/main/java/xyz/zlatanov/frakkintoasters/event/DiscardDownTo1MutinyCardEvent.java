package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

public record DiscardDownTo1MutinyCardEvent(int playerNumber, MutinyCard cardToKeep) implements PlayerEvent {

}
