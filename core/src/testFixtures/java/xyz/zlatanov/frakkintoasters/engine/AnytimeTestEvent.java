package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.AnytimeEvent;

// Something a player does at any pause: he spends his miracle token, which he can do only while he has it. Test-only,
// like ScriptEvent.
public record AnytimeTestEvent(int playerNumber) implements AnytimeEvent {
}
