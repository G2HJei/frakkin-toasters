package xyz.zlatanov.frakkintoasters.event.board;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

// Gain resources, e.g. "+1 food" on a crisis card.
public record GainResourceEvent(Resource resource, int amount) implements Event {
}
