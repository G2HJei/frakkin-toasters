package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

public record ForceTheirHandEvent(int playerNumber, SkillCard skillCard) implements PlayerEvent {
}
