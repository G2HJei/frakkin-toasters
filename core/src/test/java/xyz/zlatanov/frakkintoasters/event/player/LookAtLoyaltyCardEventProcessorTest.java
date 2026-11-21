package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.loyalty.ResolveFinalFiveCardEvent;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.card.SeenLoyaltyCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.FINAL_FIVE_DAMAGE_GALACTICA_TWICE;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.DETECTOR_SABOTAGE;

class LookAtLoyaltyCardEventProcessorTest extends EventTestHarness<LookAtLoyaltyCardEvent> {

    @Test
    void shouldLookAtACardOfTheTarget() {
        player(2).loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG);

        execute(new LookAtLoyaltyCardEvent(1, 2));

        assertEquals(List.of(new SeenLoyaltyCard(2, CYLON_SEND_TO_BRIG)), player(1).seenLoyaltyCards());
        assertEquals(List.of(), player(2).seenLoyaltyCards());
    }

    // 1 random Loyalty Card, which only the player looking at it knows.
    @Test
    void shouldLookAtOneOfTheCards() {
        player(2).loyaltyCards().addOnTop(NOT_CYLON, CYLON_SEND_TO_BRIG);

        execute(new LookAtLoyaltyCardEvent(1, 2));

        assertEquals(1, player(1).seenLoyaltyCards().size());
        assertEquals(2, player(2).loyaltyCards().size());
    }

    @Test
    void shouldSeeNothingWhenTheTargetHasNoCard() {
        execute(new LookAtLoyaltyCardEvent(1, 2));

        assertEquals(List.of(), player(1).seenLoyaltyCards());
    }

    // Another player's look at a Final Five card makes it resolve.
    @Test
    void shouldResolveAFinalFiveCardItSees() {
        player(2).loyaltyCards().addOnTop(FINAL_FIVE_DAMAGE_GALACTICA_TWICE);

        execute(new LookAtLoyaltyCardEvent(1, 2));

        assertFollowup(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_DAMAGE_GALACTICA_TWICE, 1));
    }

    @Test
    void notWhenHeLooksAtHisOwnCard() {
        player(1).loyaltyCards().addOnTop(FINAL_FIVE_DAMAGE_GALACTICA_TWICE);

        execute(new LookAtLoyaltyCardEvent(1, 1));

        assertEquals(1, player(1).seenLoyaltyCards().size());
    }

    // "Players may not look at other players' Loyalty Cards."
    @Test
    void shouldNotLookWhileDetectorSabotageIsInPlay() {
        player(2).loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG);
        ops.keepCrisisCardInPlay(DETECTOR_SABOTAGE);

        execute(new LookAtLoyaltyCardEvent(1, 2));

        assertEquals(List.of(), player(1).seenLoyaltyCards());
    }
}
