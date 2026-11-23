package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.player.DiscardKind;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FOR_LOVE;

// Sharon "Athena" Agathon's "For Love" (once per turn): when another player is made to discard Skill Cards, other than
// down to his hand limit, she draws 1 Treachery card and he discards 1 fewer.
@Source(ability = FOR_LOVE)
public record ForLoveEvent(int playerNumber, int discarderPlayerNumber, DiscardKind discard, int count)
        implements PlayerEvent {
}
