package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.player.DiscardKind;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FOR_LOVE;

// A player is made to discard Skill Cards in a game where another player has "For Love": she is asked first, unless
// forLoveAsked. It names no player, so it is the option to go on beside hers.
@Source(ability = FOR_LOVE)
public record ForcedDiscardEvent(int discarderPlayerNumber, DiscardKind discard, int count, boolean forLoveAsked)
        implements Event {
}
