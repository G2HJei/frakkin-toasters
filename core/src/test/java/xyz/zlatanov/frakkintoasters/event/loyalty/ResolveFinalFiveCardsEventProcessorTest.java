package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;

class ResolveFinalFiveCardsEventProcessorTest extends EventTestHarness<ResolveFinalFiveCardsEvent> {

    @Test
    void shouldResolveTheLastCard() {
        execute(new ResolveFinalFiveCardsEvent(2, List.of(FINAL_FIVE_DAMAGE_GALACTICA_TWICE), 1));

        assertFollowup(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_DAMAGE_GALACTICA_TWICE, 1));
    }

    @Test
    void shouldOfferTheNextOfSeveral() {
        execute(new ResolveFinalFiveCardsEvent(2, List.of(FINAL_FIVE_DAMAGE_GALACTICA_TWICE, FINAL_FIVE_CYLON_SHIPS_ACTIVATE), 1));

        assertFollowup(one(
                all(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_DAMAGE_GALACTICA_TWICE, 1),
                        new ResolveFinalFiveCardsEvent(2, List.of(FINAL_FIVE_CYLON_SHIPS_ACTIVATE), 1)),
                all(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_CYLON_SHIPS_ACTIVATE, 1),
                        new ResolveFinalFiveCardsEvent(2, List.of(FINAL_FIVE_DAMAGE_GALACTICA_TWICE), 1))));
    }

    @Test
    void shouldRejectCardsThatAreNotFinalFive() {
        assertInvalid(new ResolveFinalFiveCardsEvent(2, List.of(NOT_CYLON), 1));
    }

    @Test
    void shouldRejectNoCards() {
        assertInvalid(new ResolveFinalFiveCardsEvent(2, List.of(), 1));
    }
}
