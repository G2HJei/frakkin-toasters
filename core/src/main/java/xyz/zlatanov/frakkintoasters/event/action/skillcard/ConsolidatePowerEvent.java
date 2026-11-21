package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Consolidate Power - color: POLITICS, values: 1, 2
// Action: the player draws 2 Skill Cards of any types, also from outside his skill set.
public record ConsolidatePowerEvent(int playerNumber, SkillCard card) implements SkillCardEvent, ActionEvent {
}
