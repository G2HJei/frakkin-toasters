package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "Gas Cloud": "The Admiral may look at the top 3 cards of the Crisis deck, then place them on the top or bottom of the
// deck in any order."
public record GasCloudEvent(int playerNumber) implements PlayerEvent {
}
