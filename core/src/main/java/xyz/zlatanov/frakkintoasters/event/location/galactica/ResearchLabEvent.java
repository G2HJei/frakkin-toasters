package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

public record ResearchLabEvent(int playerNumber, SkillCardColor skillCardColor) implements PlayerEvent {

}
