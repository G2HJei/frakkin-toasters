package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record DiscardAllSkillCardsEvent(int playerNumber) implements PlayerEvent {
}
