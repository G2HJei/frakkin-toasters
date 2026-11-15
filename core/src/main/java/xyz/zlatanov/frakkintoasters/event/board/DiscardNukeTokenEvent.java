package xyz.zlatanov.frakkintoasters.event.board;

import xyz.zlatanov.frakkintoasters.event.Event;

// "Discard 1 nuke token. If you do not have any nuke tokens, you cannot choose this option."
public record DiscardNukeTokenEvent() implements Event {
}
