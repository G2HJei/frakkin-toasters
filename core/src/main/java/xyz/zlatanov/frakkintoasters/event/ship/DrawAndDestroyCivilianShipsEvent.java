package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// Draw civilian ships from the pile and destroy them, e.g. "draw a civilian ship and destroy it."
public record DrawAndDestroyCivilianShipsEvent(int count) implements Event {
}
