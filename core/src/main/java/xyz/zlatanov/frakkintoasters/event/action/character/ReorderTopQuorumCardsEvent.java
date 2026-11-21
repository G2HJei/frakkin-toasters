package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import java.util.List;

// The new order of the top Quorum Cards, the first on top. It has to hold the cards that are there.
public record ReorderTopQuorumCardsEvent(int playerNumber, List<QuorumCard> order) implements PlayerEvent {
}
