package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Event;

// What the die decides after "Encourage Mutiny": the player becomes Admiral, or the fleet loses 1 morale. Then the card
// is discarded.
public record EncourageMutinyResultEvent(int targetPlayerNumber) implements Event {
}
