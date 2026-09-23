package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

public record PlaySkillsEvent(int playerNumber, SkillCard... skillCards) implements PlayerEvent {
}
