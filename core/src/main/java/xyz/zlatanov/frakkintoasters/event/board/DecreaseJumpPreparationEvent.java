package xyz.zlatanov.frakkintoasters.event.board;

import xyz.zlatanov.frakkintoasters.event.Event;

// Decrease the Jump Preparation track by 1 (move the fleet token 1 space towards the start of the track).
public record DecreaseJumpPreparationEvent() implements Event {
}
