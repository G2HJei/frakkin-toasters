package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The President discards "Probation" to look at the cards its holder just put into the skill check.
//
// @param count how many cards the holder put in, which everyone saw: they are the top of the check's pile
public record UseProbationEvent(int playerNumber, int holderPlayerNumber, int count) implements PlayerEvent {
}
