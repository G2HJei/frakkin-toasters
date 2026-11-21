package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Test the Limits - color: ENGINEERING, value: 5
// Action: only while the fleet token isn't on a blue space of the Jump Preparation track. Advance it by 1 and roll a
// die. On 5 or lower, damage Galactica.
public record TestTheLimitsEvent(int playerNumber, SkillCard card) implements SkillCardEvent, ActionEvent {
}
