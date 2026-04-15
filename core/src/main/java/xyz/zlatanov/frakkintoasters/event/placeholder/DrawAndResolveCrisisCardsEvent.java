package xyz.zlatanov.frakkintoasters.event.placeholder;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record DrawAndResolveCrisisCardsEvent(int playerNumber) implements PlayerEvent {
}
