package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.THE_FARM;

class ResurrectionShipActionEventTest extends EventTest {

    @Test
    void shouldDrawSuperCrisisCard() {
        superCrisisDeck.nextCard(THE_FARM);
        execute(new ResurrectionShipActionEvent(1));
        assertEquals(List.of(THE_FARM), player(1).superCrisisCards().cards());
    }
}
