package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "Chief" Galen Tyrol's "Maintenance Engineer" (once per turn): on his turn, after he plays a "Repair", he takes another
// action.
public record MaintenanceEngineerEvent(int playerNumber) implements PlayerEvent {
}
