package xyz.zlatanov.frakkintoasters.event.board;

import xyz.zlatanov.frakkintoasters.event.Event;

// Place centurion markers at the start of the Boarding Party track.
public record PlaceCenturionsEvent(int count) implements Event {
}
