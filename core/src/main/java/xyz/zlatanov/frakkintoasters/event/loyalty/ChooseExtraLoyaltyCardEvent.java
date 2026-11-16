package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "No Mutineer" case of the Sleeper Agent phase: the current player chooses a human player to draw an additional card
// from the Loyalty deck (rule 5.5.2.3). If it is the Mutineer card, he doesn't draw a further one.
public record ChooseExtraLoyaltyCardEvent(int playerNumber, int targetPlayerNumber) implements PlayerEvent {
}
