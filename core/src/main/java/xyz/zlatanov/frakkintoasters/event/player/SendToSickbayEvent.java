package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.Event;

// Send the character of targetPlayerNumber to "Sickbay". Who decides is up to the wrapping PlayerDecisionEvent.
public record SendToSickbayEvent(int targetPlayerNumber) implements Event {
}
