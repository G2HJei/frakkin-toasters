package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The civilian ship "Quarantine" replaces.
public record QuarantineChoiceEvent(int playerNumber, int civilianShipId) implements PlayerEvent {
}
