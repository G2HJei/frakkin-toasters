package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// Laura Roslin's "Skilled Politician", once per game, President or not: she draws 4 Quorum Cards, resolves one
// and puts the others at the bottom of the deck (SkilledPoliticianChoiceEvent).
public record SkilledPoliticianEvent(int playerNumber) implements CharacterActionEvent {
}
