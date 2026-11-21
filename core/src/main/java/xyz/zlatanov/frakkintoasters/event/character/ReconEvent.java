package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Sharon "Boomer" Valerii's "Recon": at the end of her turn she may look at the top Crisis Card and leave it there or put
// it at the bottom.
public record ReconEvent(int playerNumber) implements PlayerEvent {
}
