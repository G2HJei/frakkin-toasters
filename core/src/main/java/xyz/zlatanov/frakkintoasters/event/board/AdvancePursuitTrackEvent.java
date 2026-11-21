package xyz.zlatanov.frakkintoasters.event.board;

import xyz.zlatanov.frakkintoasters.event.Event;

// The Cylon pursuit marker moves one space on the Pursuit track (rule 11.4.3).
public record AdvancePursuitTrackEvent() implements Event {
}
