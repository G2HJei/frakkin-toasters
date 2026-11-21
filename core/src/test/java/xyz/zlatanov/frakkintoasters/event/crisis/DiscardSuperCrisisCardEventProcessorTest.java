package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.BOMB_ON_COLONIAL_1;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.FIGHTING_BLIND;

class DiscardSuperCrisisCardEventProcessorTest extends EventTestHarness<DiscardSuperCrisisCardEvent> {

    @Test
    void shouldDiscardTheCard() {
        execute(new DiscardSuperCrisisCardEvent(FIGHTING_BLIND));
        assertEquals(List.of(FIGHTING_BLIND), superCrisisDeck.discardedCards());
    }

    @Test
    void shouldLeaveACardKeptInPlayOutOfTheDiscardPile() {
        ops.keepCrisisCardInPlay(BOMB_ON_COLONIAL_1.skillCheck());

        execute(new DiscardSuperCrisisCardEvent(BOMB_ON_COLONIAL_1));

        assertTrue(superCrisisDeck.discardedCards().isEmpty());
    }
}
