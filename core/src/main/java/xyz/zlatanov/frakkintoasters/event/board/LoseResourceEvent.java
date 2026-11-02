package xyz.zlatanov.frakkintoasters.event.board;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

// Lose resources, e.g. "-1 morale" on a crisis card.
public record LoseResourceEvent(Resource resource, int amount) implements Event {
}
