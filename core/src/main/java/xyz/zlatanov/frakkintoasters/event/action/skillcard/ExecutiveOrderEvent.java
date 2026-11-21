package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Executive Order - color: LEADERSHIP, values: 1, 2
// Action: another player of your choice either moves and then takes 1 action, or takes 2 actions without moving. Limit
// of 1 "Executive Order" per turn.
public record ExecutiveOrderEvent(int playerNumber, SkillCard card, Integer targetPlayerNumber)
        implements SkillCardEvent, ActionEvent {
}
