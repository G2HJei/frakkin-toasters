package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

// Repair - color: ENGINEERING, values: 1, 2
// Action: repair the location you are in, or, in the "Hangar Deck", up to 2 damaged vipers.
//
// @param viperIds the damaged vipers to repair, or none to repair the location
public record RepairEvent(int playerNumber, SkillCard card, List<Integer> viperIds) implements SkillCardEvent, ActionEvent {
}
