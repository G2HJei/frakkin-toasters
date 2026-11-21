package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;

import java.util.List;

// The player who decides the destination, the Admiral or the Mission Specialist, chooses one of the Destination Cards
// drawn. The others go to the bottom of the deck in the order drawn. It is only ever an option the server builds.
public record ChooseDestinationEvent(int playerNumber, DestinationCard chosen, List<DestinationCard> others)
        implements PlayerEvent {
}
