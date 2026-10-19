package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.Map;

public record TiebreakCardsBeforeSkillCheckEvent(Map<SkillCardType, Integer> tiebreakWinners) implements Event {
}
