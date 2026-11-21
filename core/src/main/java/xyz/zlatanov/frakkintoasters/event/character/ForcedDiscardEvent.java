package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.player.DiscardKind;

// A player is made to discard Skill Cards in a game where another player has "For Love": she is asked first, unless
// forLoveAsked. It names no player, so it is the option to go on beside hers.
public record ForcedDiscardEvent(int discarderPlayerNumber, DiscardKind discard, int count, boolean forLoveAsked)
        implements Event {
}
