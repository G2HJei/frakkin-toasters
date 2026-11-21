package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Encourage Mutiny (Quorum Card)
// Action: another player, not the Admiral, rolls a die. On 3 or more he becomes Admiral, otherwise the fleet loses 1
// morale. Then the card is discarded.
public record EncourageMutinyEvent(int playerNumber, int targetPlayerNumber) implements QuorumCardEvent {
}
