package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.A_GUILTY_VERDICT;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;

class PlaceCrisisCardOnBottomEventProcessorTest extends EventTestHarness<PlaceCrisisCardOnBottomEvent> {

    @Test
    void shouldPlaceCardAtBottom() {
        crisisDeck.clear().addOnTop(A_GUILTY_VERDICT);
        execute(new PlaceCrisisCardOnBottomEvent(1, DETENTE));
        assertEquals(DETENTE, crisisDeck.cards().getLast());
    }

}