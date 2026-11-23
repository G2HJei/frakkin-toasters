package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MAINTENANCE_ENGINEER;

// "Chief" Galen Tyrol's "Maintenance Engineer" (once per turn): on his turn, after he plays a "Repair", he takes another
// action.
@Source(ability = MAINTENANCE_ENGINEER)
public record MaintenanceEngineerEvent(int playerNumber) implements PlayerEvent {
}
