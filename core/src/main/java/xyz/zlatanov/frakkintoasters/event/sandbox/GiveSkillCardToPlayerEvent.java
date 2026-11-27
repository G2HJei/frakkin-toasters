package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

public record GiveSkillCardToPlayerEvent(int targetPlayerNumber, SkillCard card) implements SandboxEvent {
}
