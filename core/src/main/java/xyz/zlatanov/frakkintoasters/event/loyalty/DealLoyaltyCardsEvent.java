package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Deals Loyalty Cards from the top of the Loyalty deck to the player, e.g. in the Sleeper Agent phase or after an
// execution. A human who is dealt the "You Are the Mutineer" card reveals it (see RevealMutineerEvent).
//
// @param extra the cards are an additional draw, so a Mutineer card among them doesn't draw one more (rules 5.2.1 and
//              5.5.2.3)
public record DealLoyaltyCardsEvent(int playerNumber, int count, boolean extra) implements PlayerEvent {
}
