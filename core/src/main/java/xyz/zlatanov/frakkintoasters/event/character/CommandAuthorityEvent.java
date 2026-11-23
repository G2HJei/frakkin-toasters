package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.COMMAND_AUTHORITY;

// William Adama's "Command Authority" (once per game): once a skill check is resolved, he takes its cards into his
// hand instead of discarding them.
@Source(ability = COMMAND_AUTHORITY)
public record CommandAuthorityEvent(int playerNumber) implements PlayerEvent {
}
