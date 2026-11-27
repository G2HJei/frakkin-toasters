package xyz.zlatanov.frakkintoasters.event.board;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

public record GainResourceEvent(Resource resource, int amount) implements Event {
}
