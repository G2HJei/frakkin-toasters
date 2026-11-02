package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.event.Event;

// Defending a Prisoner (Pegasus crisis)
// FAIL: -1 morale and roll a die. If 4 or lower, the current player is executed.
// Covers the die roll and the execution (an ExecuteEvent). The morale is lost by a separate LoseResourceEvent.
public record DefendingAPrisonerFailEvent() implements Event {
}
