package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import java.util.List;

// Resignation (Quorum Card)
// Action: the President discards any number of Quorum Cards and draws as many, then gives the President title to
// another character. Then the card is discarded.
//
// @param discards the Quorum Cards to discard, maybe none
public record ResignationEvent(int playerNumber, List<QuorumCard> discards) implements QuorumCardEvent {
}
