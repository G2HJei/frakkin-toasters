package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The Chief of Staff discards "Assign Chief of Staff", so the Politics Cards in the check count positive.
public record UseChiefOfStaffEvent(int playerNumber) implements PlayerEvent {
}
