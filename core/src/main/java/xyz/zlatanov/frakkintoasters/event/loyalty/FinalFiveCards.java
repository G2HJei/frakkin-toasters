package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;

/**
 * What a look at another player's Loyalty Cards does when it finds Final Five cards (rule 5.4.1.1).
 */
@NoArgsConstructor(access = PRIVATE)
public final class FinalFiveCards {

    /**
     * @return the Final Five cards among the cards seen, resolved in the order the current player chooses.
     * {@link Followup#NONE} when there are none, or the player looked at his own cards
     */
    public static Followup seen(int lookerPlayerNumber, int holderPlayerNumber, List<LoyaltyCard> cards) {
        if (lookerPlayerNumber == holderPlayerNumber) {
            return NONE; // only another player's look resolves the card
        }
        return inOrder(holderPlayerNumber, cards.stream().filter(LoyaltyCard::isFinalFive).toList(), lookerPlayerNumber);
    }

    static Followup inOrder(int holderPlayerNumber, List<LoyaltyCard> cards, int lookerPlayerNumber) {
        if (cards.isEmpty()) {
            return NONE;
        }
        if (cards.size() == 1) {
            return single(new ResolveFinalFiveCardEvent(holderPlayerNumber, cards.getFirst(), lookerPlayerNumber));
        }
        // Several cards resolve in the order the current player chooses: he picks the first, then the next of the rest.
        return one(cards.stream().distinct().map(card -> {
            val rest = new ArrayList<>(cards);
            rest.remove(card);
            return all(new ResolveFinalFiveCardEvent(holderPlayerNumber, card, lookerPlayerNumber),
                    new ResolveFinalFiveCardsEvent(holderPlayerNumber, rest, lookerPlayerNumber));
        }).toArray(Followup[]::new));
    }
}
