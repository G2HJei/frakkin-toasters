package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.THE_FARM;

class ResurrectionShipEventProcessorTest extends EventTestHarness<ResurrectionShipEvent> {

    @Test
    void shouldDrawSuperCrisisCard() {
        nextCard(superCrisisDeck, THE_FARM);
        executeAndAssertNoFollowup(new ResurrectionShipEvent(1));
        assertEquals(List.of(THE_FARM), player(1).superCrisisCards().cards());
    }
}
