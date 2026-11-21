package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;

// A Super Crisis Card that has resolved goes to the Super Crisis discard pile, unless it stays in play.
public record DiscardSuperCrisisCardEvent(SuperCrisisCard card) implements Event {
}
