package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

// Consult the Oracle (Quorum Card)
// Action: look at the bottom card of a deck, then at every card of the Destiny deck. Discard 2 of them and shuffle the
// Destiny deck. Then the card is discarded.
public record ConsultTheOracleEvent(int playerNumber, DeckId deck) implements QuorumCardEvent {
}
