package xyz.zlatanov.frakkintoasters.event.placeholder;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record PlaySuperCrisisCardEvent(int playerNumber) implements PlayerEvent {
}
