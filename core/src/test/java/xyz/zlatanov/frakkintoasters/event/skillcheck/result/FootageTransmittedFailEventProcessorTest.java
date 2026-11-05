package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_DAMAGE_GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.FIGHTING_BLIND;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.INBOUND_NUKES;

class FootageTransmittedFailEventProcessorTest extends EventTestHarness<FootageTransmittedFailEvent> {

    @Test
    void shouldGiveEachRevealedCylonSuperCrisisCard() {
        revealCylon();
        player(3).loyaltyCards()
                .addOnTop(CYLON_DAMAGE_GALACTICA)
                .reveal(CYLON_DAMAGE_GALACTICA);
        superCrisisDeck.nextCard(FIGHTING_BLIND, INBOUND_NUKES);

        execute(new FootageTransmittedFailEvent());

        assertEquals(List.of(FIGHTING_BLIND), player(1).superCrisisCards().cards());
        assertTrue(player(2).superCrisisCards().isEmpty());
        assertEquals(List.of(INBOUND_NUKES), player(3).superCrisisCards().cards());
    }

    @Test
    void shouldDoNothingWithoutRevealedCylons() {
        execute(new FootageTransmittedFailEvent());

        game.players().forEach(p -> assertTrue(p.superCrisisCards().isEmpty()));
    }
}
