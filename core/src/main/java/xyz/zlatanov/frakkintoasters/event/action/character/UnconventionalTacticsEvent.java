package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.UNCONVENTIONAL_TACTICS;

// Tom Zarek's "Unconventional Tactics", once per game: -1 population, +1 of another resource.
@Source(ability = UNCONVENTIONAL_TACTICS)
public record UnconventionalTacticsEvent(int playerNumber, Resource resource) implements CharacterActionEvent {
}
