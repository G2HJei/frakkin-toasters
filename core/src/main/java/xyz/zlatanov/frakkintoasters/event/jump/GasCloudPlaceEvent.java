package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import java.util.List;

// The Admiral places the next of the cards he looked at on the top or the bottom of the Crisis deck. The cards go on one
// after the other, so the order he picks them in is the order of the deck.
public record GasCloudPlaceEvent(int playerNumber, List<CrisisCard> cards) implements PlayerEvent {
    public GasCloudPlaceEvent {
        cards = List.copyOf(cards);
    }
}
