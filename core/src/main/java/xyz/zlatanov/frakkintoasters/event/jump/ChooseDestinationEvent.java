package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;

// The Admiral chooses the Destination Card the fleet jumps to, out of the two cards drawn. The other goes to the bottom
// of the deck.
public record ChooseDestinationEvent(int playerNumber, DestinationCard chosen, DestinationCard other) implements PlayerEvent {
}
