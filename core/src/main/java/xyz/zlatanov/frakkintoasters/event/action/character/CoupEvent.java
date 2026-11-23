package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.COUP;

// Felix Gaeta's "Coup", once per game: he becomes Admiral, moving from the "Brig" to "Command" first if he is
// there.
@Source(ability = COUP)
public record CoupEvent(int playerNumber) implements CharacterActionEvent {
}
