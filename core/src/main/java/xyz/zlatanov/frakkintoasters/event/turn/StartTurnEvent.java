package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.event.Event;

// Starts the current player's turn and queues its steps (rule 8.1), then the end of the turn.
public record StartTurnEvent() implements Event {
}
