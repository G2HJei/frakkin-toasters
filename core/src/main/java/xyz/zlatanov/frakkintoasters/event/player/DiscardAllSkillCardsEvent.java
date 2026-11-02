package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The player discards his whole hand of Skill Cards.
public record DiscardAllSkillCardsEvent(int playerNumber) implements PlayerEvent {
}
