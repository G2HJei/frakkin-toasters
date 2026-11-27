package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.Event;

// Who decides is up to the wrapping PlayerDecisionEvent.
public record SendToSickbayEvent(int targetPlayerNumber) implements Event {
}
