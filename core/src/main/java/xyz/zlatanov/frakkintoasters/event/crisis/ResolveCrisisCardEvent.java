package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

// Resolve a Crisis Card, e.g. the one the current player draws during the Crisis step (rule 8.5).
public record ResolveCrisisCardEvent(CrisisCard card) implements Event {
}
