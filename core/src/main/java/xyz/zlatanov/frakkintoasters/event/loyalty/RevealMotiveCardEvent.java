package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.AnytimeEvent;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;

// A Cylon Leader reveals one of his Motive Cards while its requirement is met, at any pause of the game, even in the
// middle of resolving something (rule 6.2.2.1).
public record RevealMotiveCardEvent(int playerNumber, MotiveCard card) implements AnytimeEvent {
}
