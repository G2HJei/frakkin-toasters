package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Dogfight - color: PILOTING, values: 0
// Skill Check: The current player may damage 1 viper in a space area or in the "Reserves" to remove 1 other card from
// this skill check.
public record DogfightEvent(int playerNumber, Integer viperId, SkillCard removedCard) implements PlayerEvent {
}
