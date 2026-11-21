package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Release Cylon Mugshots (Quorum Card)
// Action: look at a random Loyalty Card of another player, then roll a die. On 3 or less the fleet loses 1 morale.
// Then the card is discarded.
public record ReleaseCylonMugshotsEvent(int playerNumber, int targetPlayerNumber) implements QuorumCardEvent {
}
