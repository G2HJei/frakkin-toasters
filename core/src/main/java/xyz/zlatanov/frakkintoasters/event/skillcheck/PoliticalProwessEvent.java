package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Political Prowess - color: POLITICS, values: 6
// Play before making a skill check triggered by a location. Do not make the skill check; instead, it automatically
// passes or fails (your choice).
public record PoliticalProwessEvent(int playerNumber, SkillCard politicalProwessCard,
                                    boolean pass) implements PlayerEvent {
}
