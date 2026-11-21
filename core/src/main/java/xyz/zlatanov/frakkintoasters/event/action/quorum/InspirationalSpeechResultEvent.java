package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Event;

// What the die decides after an "Inspirational Speech": 1 morale and the card leaves the game, or the card is discarded.
public record InspirationalSpeechResultEvent() implements Event {
}
