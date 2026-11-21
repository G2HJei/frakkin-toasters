package xyz.zlatanov.frakkintoasters.event.deck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

/**
 * The player looks at the top card of the Crisis or the Destination deck, then puts it back on the top or on the
 * bottom, e.g. after a "Launch Scout". It is an option the server builds, never the answer to a decision.
 */
public record LookAtTopCardEvent(int playerNumber, DeckId deck) implements PlayerEvent {
}
