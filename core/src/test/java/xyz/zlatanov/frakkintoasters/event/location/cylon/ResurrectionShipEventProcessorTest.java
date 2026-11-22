package xyz.zlatanov.frakkintoasters.event.location.cylon;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESURRECTION_SHIP;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAVIL;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.THE_FARM;

class ResurrectionShipEventProcessorTest extends EventTestHarness<ResurrectionShipEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(CAVIL);
        moveTo(RESURRECTION_SHIP, CAVIL);
    }

    @Test
    void shouldDrawSuperCrisisCard() {
        superCrisisDeck.nextCard(THE_FARM);
        execute(new ResurrectionShipEvent(1));
        assertEquals(List.of(THE_FARM), player(1).superCrisisCards().cards());
    }

    // Every Super Crisis Card is in a hand: there is none to draw (F-b894d2).
    @Test
    void notWithoutASuperCrisisCardLeft() {
        superCrisisDeck.clear();

        assertFalse(ResurrectionShipEventProcessor.canAct(game));
        assertInvalid(new ResurrectionShipEvent(1));
    }
}
