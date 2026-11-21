package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Popular Influence - color: POLITICS
// Action: draw 2 Quorum Cards and give one to the President. Then play the other one, or discard it.
public record PopularInfluenceEvent(int playerNumber, SkillCard card) implements SkillCardEvent, ActionEvent {
}
