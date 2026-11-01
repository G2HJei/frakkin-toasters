package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record GiveTitlesToNextInLineEvent(int playerNumber) implements PlayerEvent {
}
