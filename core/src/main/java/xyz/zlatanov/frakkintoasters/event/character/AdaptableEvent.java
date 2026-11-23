package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.ADAPTABLE;

// Tory Foster's "Adaptable": after any player uses a Quorum Card's action, she may draw 2 Skill Cards of any types,
// from outside her skill set too.
@Source(ability = ADAPTABLE)
public record AdaptableEvent(int playerNumber) implements PlayerEvent {
}
