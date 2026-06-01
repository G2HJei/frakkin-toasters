package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.A_GUILTY_VERDICT;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;

class PlaceCrisisCardOnTopEventProcessorTest extends EventTestHarness<PlaceCrisisCardOnTopEvent> {

    @Test
    void shouldPlaceCardOnBottom() {
        clear(crisisDeck).addOnTop(A_GUILTY_VERDICT);
        execute(new PlaceCrisisCardOnTopEvent(1, DETENTE));
        assertEquals(DETENTE, crisisDeck.cards().getFirst());
    }
}