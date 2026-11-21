package xyz.zlatanov.frakkintoasters.state.deck;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import static org.junit.jupiter.api.Assertions.assertEquals;

// The decks hold as many cards as the Appendix counts.
class DecksHolderTest {

    @Test
    void theCrisisDeckHas141Cards() {
        val decks = DecksHolder.builder().build();

        assertEquals(141, decks.crisisCardsLeft()); // A2.3.9
        assertEquals(4, count(decks, CrisisCard.FOOD_SHORTAGE));
        assertEquals(3, count(decks, CrisisCard.WATER_SHORTAGE_BASESTAR_AND_JUMP));
    }

    @Test
    void theQuorumDeckHas29Cards() {
        assertEquals(29, DecksHolder.builder().build().quorumCardsLeft()); // A2.3.13
    }

    private static long count(DecksHolder decks, CrisisCard card) {
        return decks.<CrisisCard>deck(DeckId.CRISIS).cards().stream().filter(card::equals).count();
    }
}
