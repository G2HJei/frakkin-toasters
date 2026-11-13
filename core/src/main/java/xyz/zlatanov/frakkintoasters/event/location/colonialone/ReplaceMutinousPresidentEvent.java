package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The rest of the "Administration" action: if the President has any Mutiny Cards, the player chooses a player to gain
// the President title.
public record ReplaceMutinousPresidentEvent(int playerNumber) implements PlayerEvent {
}
