package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.BINARY_STAR;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.MISJUMP;

class PlaceDestinationCardOnTopEventProcessorTest extends EventTestHarness<PlaceDestinationCardOnTopEvent> {
    @Test
    void shouldPlaceCardOnTop() {
        clear(destinationDeck).addOnTop(MISJUMP);
        execute(new PlaceDestinationCardOnTopEvent(1, BINARY_STAR));
        assertEquals(BINARY_STAR, destinationDeck.cards().getFirst());
    }

}