package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Build Nuke - color: ENGINEERING, value: 6
// Action: the Admiral gains 1 nuke token.
public record BuildNukeEvent(int playerNumber, SkillCard card) implements SkillCardEvent, ActionEvent {
}
