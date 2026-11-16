package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ChooseExtraLoyaltyCardEventProcessorTest extends EventTestHarness<ChooseExtraLoyaltyCardEvent> {

    @BeforeEach
    void players() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
    }

    @Test
    void theChosenHumanDrawsOneMoreCard() {
        execute(new ChooseExtraLoyaltyCardEvent(1, 3));

        assertFollowup(new DealLoyaltyCardsEvent(3, 1, true));
    }

    @Test
    void notARevealedCylon() {
        player(3).loyaltyCards().addOnTop(xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG)
                .reveal(xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG);

        assertInvalid(new ChooseExtraLoyaltyCardEvent(1, 3));
    }
}
