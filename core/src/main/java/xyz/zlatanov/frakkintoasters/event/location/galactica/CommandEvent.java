package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Command (Galactica location)
// Action: Activate up to 2 unmanned vipers.
public record CommandEvent(int playerNumber) implements PlayerEvent {
}
