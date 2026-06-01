package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

public record Discard1MutinyCardEvent(int playerNumber, MutinyCard cardToDiscard) implements PlayerEvent {

}
