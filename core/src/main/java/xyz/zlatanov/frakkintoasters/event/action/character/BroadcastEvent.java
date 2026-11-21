package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

// Gaius Baltar (alt.)'s "Broadcast": he spends 3 miracle tokens to raise a resource by 2.
public record BroadcastEvent(int playerNumber, Resource resource) implements CharacterActionEvent {
}
