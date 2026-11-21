package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Karl "Helo" Agathon (alt.)'s "No One Gets Left Behind" (once per game): during the ships' removal of a jump, 1 fuel is
// lost and 2 population gained.
public record NoOneGetsLeftBehindEvent(int playerNumber) implements PlayerEvent {
}
