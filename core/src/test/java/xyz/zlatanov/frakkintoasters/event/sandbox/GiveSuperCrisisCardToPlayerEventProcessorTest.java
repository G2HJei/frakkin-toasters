package xyz.zlatanov.frakkintoasters.event.sandbox;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.THE_FARM;

class GiveSuperCrisisCardToPlayerEventProcessorTest extends EventTestHarness<GiveSuperCrisisCardToPlayerEvent> {

    @Test
    void theCardComesOutOfTheDrawPile() {
        superCrisisDeck.clear();
        superCrisisDeck.addOnTop(THE_FARM);

        execute(new GiveSuperCrisisCardToPlayerEvent(1, THE_FARM));

        assertEquals(List.of(THE_FARM), player(1).superCrisisCards().cards());
        assertFalse(superCrisisDeck.cards().contains(THE_FARM));
    }

    @Test
    void notACardThatIsNotInTheDrawPile() {
        superCrisisDeck.clear();

        assertInvalid(new GiveSuperCrisisCardToPlayerEvent(1, THE_FARM));
    }
}
