package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The player discards random Skill Cards, e.g. "The current player discards 3 random Skill Cards."
public record DiscardRandomSkillCardsEvent(int playerNumber, int count) implements PlayerEvent {
}
