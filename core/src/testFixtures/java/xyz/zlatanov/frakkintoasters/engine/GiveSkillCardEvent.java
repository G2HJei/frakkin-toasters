package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Puts a chosen card into a player's hand for an engine test, where a seeded draw can't pick one. The card doesn't come
// out of its deck.
public record GiveSkillCardEvent(int playerNumber, SkillCard card) implements Event {
}
