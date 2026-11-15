package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import xyz.zlatanov.frakkintoasters.event.Event;

// What "Pegasus CIC" does to the chosen basestar, after the die roll:
// 1-3: Damage Pegasus
// 4-6: Damage basestar
// 7-8: Damage basestar twice
public record PegasusCicResultEvent(int basestarId) implements Event {
}
