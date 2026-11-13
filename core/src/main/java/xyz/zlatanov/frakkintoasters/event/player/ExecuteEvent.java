package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Execute the player's character, e.g. after passing the "Airlock" skill check (rule 7.1). The player of the executed
// character carries out the steps, so he is the player of the event.
public record ExecuteEvent(int playerNumber) implements PlayerEvent {
}
