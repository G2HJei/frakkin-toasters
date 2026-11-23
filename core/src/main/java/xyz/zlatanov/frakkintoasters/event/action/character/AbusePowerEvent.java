package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.ABUSE_POWER;

// Tom Zarek (alt.)'s "Abuse Power", once per game: he draws 4 Mutiny Cards, plays one and puts the others at the
// bottom of the deck (AbusePowerChoiceEvent). These draws don't send him to the "Brig".
@Source(ability = ABUSE_POWER)
public record AbusePowerEvent(int playerNumber) implements CharacterActionEvent {
}
