package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

public record PutSkillCardOnTopEvent(SkillCard card) implements SandboxEvent {
}
