package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

// Tom Zarek's "Unconventional Tactics", once per game: -1 population, +1 of another resource.
public record UnconventionalTacticsEvent(int playerNumber, Resource resource) implements CharacterActionEvent {
}
