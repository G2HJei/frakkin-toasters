package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;

// An executed Cylon reveals one of his "You Are a Cylon" cards, without its power (rule 7.1), then resurrects.
public record RevealCylonOnExecutionEvent(int playerNumber, LoyaltyCard card) implements PlayerEvent {
}
