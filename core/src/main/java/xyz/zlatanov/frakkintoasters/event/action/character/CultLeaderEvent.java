package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CULT_LEADER;

// Gaius Baltar (alt.)'s "Cult Leader": a player at his location gets one of his miracle tokens (give), or he
// takes that player's.
@Source(ability = CULT_LEADER)
public record CultLeaderEvent(int playerNumber, int targetPlayerNumber, boolean give) implements CharacterActionEvent {
}
