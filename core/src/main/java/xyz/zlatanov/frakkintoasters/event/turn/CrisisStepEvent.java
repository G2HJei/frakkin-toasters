package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.event.Event;

// The current player draws a Crisis Card and resolves it (rule 8.5). With "again" it is the Crisis step that a card sent
// the turn back to, which brings its own Activate Cylon Ships step.
public record CrisisStepEvent(boolean again) implements Event {

    public CrisisStepEvent() {
        this(false);
    }
}
