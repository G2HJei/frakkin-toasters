package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Event;

// What the die decides after a "Food Rationing": 1 food and the card leaves the game, or the card is discarded.
public record FoodRationingResultEvent() implements Event {
}
