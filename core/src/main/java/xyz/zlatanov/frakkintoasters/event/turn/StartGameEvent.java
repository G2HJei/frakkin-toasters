package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.event.Event;

// Sets up a game with the Kobol objective (rule A3.1), then starts the first turn.
public record StartGameEvent() implements Event {
}
