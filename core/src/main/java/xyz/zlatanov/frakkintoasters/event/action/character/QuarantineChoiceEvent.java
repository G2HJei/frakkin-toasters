package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.QUARANTINE;

// The civilian ship "Quarantine" replaces.
@Source(ability = QUARANTINE)
public record QuarantineChoiceEvent(int playerNumber, int civilianShipId) implements PlayerEvent {
}
