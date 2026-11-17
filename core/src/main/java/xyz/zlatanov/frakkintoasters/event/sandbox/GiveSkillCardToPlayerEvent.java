package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Gives the player a Skill Card out of its color's draw pile.
public record GiveSkillCardToPlayerEvent(int targetPlayerNumber, SkillCard card) implements SandboxEvent {
}
