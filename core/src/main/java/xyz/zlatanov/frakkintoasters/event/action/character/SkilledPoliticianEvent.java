package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.SKILLED_POLITICIAN;

// Laura Roslin's "Skilled Politician", once per game, President or not: she draws 4 Quorum Cards, resolves one
// and puts the others at the bottom of the deck (SkilledPoliticianChoiceEvent).
@Source(ability = SKILLED_POLITICIAN)
public record SkilledPoliticianEvent(int playerNumber) implements CharacterActionEvent {
}
