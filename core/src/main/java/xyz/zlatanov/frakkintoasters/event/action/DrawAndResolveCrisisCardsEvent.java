package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record DrawAndResolveCrisisCardsEvent(int playerNumber) implements PlayerEvent {

}
