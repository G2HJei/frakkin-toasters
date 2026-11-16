package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.GiveTitlesToNextInLineEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class RevealMutineerEventProcessorTest extends EventTestHarness<RevealMutineerEvent> {

    @BeforeEach
    void players() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        player(1).loyaltyCards().addOnTop(MUTINEER);
    }

    // "If you receive this card facedown, immediately reveal it and draw another Loyalty Card. Any time you receive this
    // card, lose your titles and draw a Mutiny Card."
    @Test
    void theMutineerDrawsAnotherLoyaltyCardLosesTitlesAndDrawsAMutinyCard() {
        execute(new RevealMutineerEvent(1, true));

        assertFollowup(all(new DealLoyaltyCardsEvent(1, 1, true), new GiveTitlesToNextInLineEvent(1),
                new DrawMutinyCardEvent(1)));
    }

    @Test
    void noAdditionalLoyaltyCardInTheNoMutineerCase() {
        execute(new RevealMutineerEvent(1, false));

        assertFollowup(all(new GiveTitlesToNextInLineEvent(1), new DrawMutinyCardEvent(1)));
    }

    @Test
    void onlyAHumanMutineerReveals() {
        assertInvalid(new RevealMutineerEvent(2, true));
    }
}
