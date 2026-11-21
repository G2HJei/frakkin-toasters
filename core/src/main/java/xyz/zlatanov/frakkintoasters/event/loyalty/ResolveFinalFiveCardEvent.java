package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;

// A player looked at another player's Final Five card: the holder shows it to everyone and its effect resolves, then he
// shuffles it back among his Loyalty Cards (rule 5.4.1.1). A plain event, so the current player orders several.
public record ResolveFinalFiveCardEvent(int holderPlayerNumber, LoyaltyCard card, int lookerPlayerNumber) implements Event {
}
