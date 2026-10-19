package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

public record PlayRecklessSkillCardEvent(int playerNumber, SkillCard recklessSkillCard) implements PlayerEvent {
}
