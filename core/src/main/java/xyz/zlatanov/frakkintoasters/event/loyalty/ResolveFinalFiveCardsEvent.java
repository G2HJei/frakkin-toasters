package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;

import java.util.List;

// The Final Five cards a look found that are still to resolve. The current player picks the next one (rule 5.4.1.1).
public record ResolveFinalFiveCardsEvent(int holderPlayerNumber, List<LoyaltyCard> cards, int lookerPlayerNumber)
        implements Event {

    public ResolveFinalFiveCardsEvent {
        cards = cards == null ? null : List.copyOf(cards);
    }
}
