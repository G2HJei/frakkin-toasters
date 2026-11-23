package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.QUARANTINE;

// Sherman "Doc" Cottle's "Quarantine", once per game: he looks at every civilian ship in space, then one of them
// is replaced by a new one and shuffled back into the deck (QuarantineChoiceEvent).
@Source(ability = QUARANTINE)
public record QuarantineEvent(int playerNumber) implements CharacterActionEvent {
}
