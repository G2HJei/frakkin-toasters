package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// Draws a basestar damage token for the basestar, which is destroyed at 3 damage (rule 11.7.3.1).
public record DamageBasestarEvent(int basestarId) implements Event {
}
