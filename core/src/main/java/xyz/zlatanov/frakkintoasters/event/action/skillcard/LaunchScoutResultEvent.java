package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// What the die decides after a "Launch Scout": the player who played it looks at a card, or a raptor is destroyed.
public record LaunchScoutResultEvent(int playerNumber) implements PlayerEvent {
}
