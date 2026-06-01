package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.BINARY_STAR;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.MISJUMP;

class PlaceDestinationCardAtBottomEventProcessorTest extends EventTestHarness<PlaceDestinationCardAtBottomEvent> {

    @Test
    void shouldPlaceCardAtBottom() {
        clear(destinationDeck).addOnTop(MISJUMP);
        execute(new PlaceDestinationCardAtBottomEvent(1, BINARY_STAR));
        assertEquals(BINARY_STAR, destinationDeck.cards().getLast());
    }
}