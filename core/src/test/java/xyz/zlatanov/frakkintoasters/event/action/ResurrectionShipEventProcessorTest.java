package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.THE_FARM;

class ResurrectionShipEventProcessorTest extends EventTestHarness<ResurrectionShipEvent> {

    @Test
    void shouldDrawSuperCrisisCard() {
        superCrisisDeck.nextCard(THE_FARM);

        execute(new ResurrectionShipEvent(1));

        assertNoFollowup();
        assertEquals(List.of(THE_FARM), player(1).superCrisisCards().cards());
    }
}
