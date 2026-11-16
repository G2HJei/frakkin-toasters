package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// When the fleet jumps, an assault raptor goes back to the "Reserves" and its pilot to the "Hangar Deck", unless its
// player chose to remain in the space area (rule 12.1.1). The player is its pilot, or the current player for an unmanned
// one.
public record ReturnAssaultRaptorEvent(int playerNumber, int shipId) implements PlayerEvent {
}
