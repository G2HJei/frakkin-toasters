package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// Gaius Baltar (alt.)'s "Cult Leader": a player at his location gets one of his miracle tokens (give), or he
// takes that player's.
public record CultLeaderEvent(int playerNumber, int targetPlayerNumber, boolean give) implements CharacterActionEvent {
}
