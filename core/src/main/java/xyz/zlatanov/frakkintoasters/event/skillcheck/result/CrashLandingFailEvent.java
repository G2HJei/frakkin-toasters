package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.event.Event;

// Crash Landing (Galactica crisis)
// FAIL: The Admiral may spend 1 fuel. If he does not, -1 morale, and the current player is sent to "Sickbay."
public record CrashLandingFailEvent() implements Event {
}
