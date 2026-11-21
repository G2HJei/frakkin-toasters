package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// State of Emergency - color: LEADERSHIP, value: 6
// Action: lose 1 food. Then each player, starting with you and going clockwise, may move or take 1 action.
public record StateOfEmergencyEvent(int playerNumber, SkillCard card) implements SkillCardEvent, ActionEvent {
}
