package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Puts the Skill Card on top of the Destiny deck, from the Destiny deck when it is there, otherwise from its color's
// draw pile, so the next skill check gets it.
public record PutDestinyCardOnTopEvent(SkillCard card) implements SandboxEvent {
}
