package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.loyalty.ResolveFinalFiveCardEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.ResolveFinalFiveCardsEvent;
import xyz.zlatanov.frakkintoasters.state.card.SeenLoyaltyCard;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.DETECTOR_SABOTAGE;

class LookAtAllLoyaltyCardsEventProcessorTest extends EventTestHarness<LookAtAllLoyaltyCardsEvent> {

    @Test
    void shouldLookAtEveryCardOfTheTarget() {
        player(2).loyaltyCards().addOnTop(NOT_CYLON, CYLON_SEND_TO_BRIG);

        execute(new LookAtAllLoyaltyCardsEvent(1, 2));

        assertEquals(Set.of(new SeenLoyaltyCard(2, CYLON_SEND_TO_BRIG), new SeenLoyaltyCard(2, NOT_CYLON)),
                Set.copyOf(player(1).seenLoyaltyCards()));
        assertEquals(2, player(2).loyaltyCards().size());
    }

    // Several Final Five cards resolve in the order the current player chooses: he picks the first.
    @Test
    void shouldLetTheCurrentPlayerOrderTheFinalFiveCards() {
        player(2).loyaltyCards().addOnTop(FINAL_FIVE_DAMAGE_GALACTICA_TWICE, NOT_CYLON, FINAL_FIVE_YOU_ARE_SENT_TO_BRIG);

        execute(new LookAtAllLoyaltyCardsEvent(3, 2));

        assertFollowup(one(
                all(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_YOU_ARE_SENT_TO_BRIG, 3),
                        new ResolveFinalFiveCardsEvent(2, List.of(FINAL_FIVE_DAMAGE_GALACTICA_TWICE), 3)),
                all(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_DAMAGE_GALACTICA_TWICE, 3),
                        new ResolveFinalFiveCardsEvent(2, List.of(FINAL_FIVE_YOU_ARE_SENT_TO_BRIG), 3))));
    }

    @Test
    void shouldNotLookAtHisOwnCards() {
        assertInvalid(new LookAtAllLoyaltyCardsEvent(1, 1));
    }

    @Test
    void shouldNotLookAtANonPlayer() {
        assertInvalid(new LookAtAllLoyaltyCardsEvent(1, 9));
    }

    // "Players may not look at other players' Loyalty Cards."
    @Test
    void shouldNotLookWhileDetectorSabotageIsInPlay() {
        player(2).loyaltyCards().addOnTop(FINAL_FIVE_DAMAGE_GALACTICA_TWICE);
        ops.keepCrisisCardInPlay(DETECTOR_SABOTAGE);

        execute(new LookAtAllLoyaltyCardsEvent(1, 2));

        assertEquals(List.of(), player(1).seenLoyaltyCards());
    }
}
