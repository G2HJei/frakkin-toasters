package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class DealLoyaltyCardsEventProcessorTest extends EventTestHarness<DealLoyaltyCardsEvent> {

    @BeforeEach
    void players() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
    }

    @Test
    void theCardsComeFromTheTopOfTheLoyaltyDeck() {
        loyaltyDeck.addOnTop(NOT_CYLON, NOT_CYLON_SELFISH);

        execute(new DealLoyaltyCardsEvent(1, 2, false));

        assertEquals(List.of(NOT_CYLON, NOT_CYLON_SELFISH), player(1).loyaltyCards().cards());
    }

    @Test
    void fewerCardsAreDealtWhenTheDeckHasFewerLeft() {
        loyaltyDeck.clear();
        loyaltyDeck.addOnTop(NOT_CYLON);

        execute(new DealLoyaltyCardsEvent(1, 2, false));

        assertEquals(List.of(NOT_CYLON), player(1).loyaltyCards().cards());
    }

    // "If a human player receives the Mutineer card facedown, he must immediately reveal the card and draw an additional
    // Loyalty Card." (rule 5.5.2.1)
    @Test
    void aHumanWhoIsDealtTheMutineerRevealsIt() {
        loyaltyDeck.addOnTop(MUTINEER);

        execute(new DealLoyaltyCardsEvent(1, 1, false));

        assertFollowup(new RevealMutineerEvent(1, true));
    }

    // "If this player then reveals the Mutineer card, he does not draw an additional Loyalty Card." (rule 5.5.2.3)
    @Test
    void theAdditionalCardOfTheNoMutineerCaseDrawsNoFurtherOne() {
        loyaltyDeck.addOnTop(MUTINEER);

        execute(new DealLoyaltyCardsEvent(1, 1, true));

        assertFollowup(new RevealMutineerEvent(1, false));
    }

    // A Cylon who is dealt the card doesn't reveal it, he passes it on (rule 5.5.2.2).
    @Test
    void aRevealedCylonDoesNotRevealTheMutineer() {
        revealCylon();
        loyaltyDeck.addOnTop(MUTINEER);

        execute(new DealLoyaltyCardsEvent(1, 1, false));

        assertEquals(List.of(MUTINEER), player(1).loyaltyCards().cards());
    }
}
