package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import xyz.zlatanov.frakkintoasters.event.Event;

// Pegasus CIC (Pegasus location)
// Action: Choose a basestar and roll a die:
// 1-3: Damage Pegasus
// 4-6: Damage basestar
// 7-8: Damage basestar twice
public record PegasusCicEvent(int basestarId) implements Event {
}
