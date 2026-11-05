package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.Event;

// Send the character of targetPlayerNumber to the "Brig". Who decides is up to the wrapping PlayerDecisionEvent.
public record SendToBrigEvent(int targetPlayerNumber) implements Event {
}
