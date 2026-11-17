package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Takes the Skill Card out of its color's draw pile and puts it on top, so the next draw of that color gives it.
public record PutSkillCardOnTopEvent(SkillCard card) implements SandboxEvent {
}
