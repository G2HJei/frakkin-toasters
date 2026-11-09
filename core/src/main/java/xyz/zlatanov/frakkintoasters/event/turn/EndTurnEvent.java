package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.event.Event;

// Ends the current player's turn: the Cylons win if a resource is at 0, otherwise the next player's turn starts.
public record EndTurnEvent() implements Event {
}
