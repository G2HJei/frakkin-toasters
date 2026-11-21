package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardKind;

// Sharon "Athena" Agathon's "For Love" (once per turn): when another player is made to discard Skill Cards, other than
// down to his hand limit, she draws 1 Treachery card and he discards 1 fewer.
public record ForLoveEvent(int playerNumber, int discarderPlayerNumber, DiscardKind discard, int count)
        implements PlayerEvent {
}
