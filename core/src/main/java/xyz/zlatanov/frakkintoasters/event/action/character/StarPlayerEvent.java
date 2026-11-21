package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.List;

// Samuel T. Anders' "Star Player": he discards Skill Cards and draws as many of one type of his skill set.
public record StarPlayerEvent(int playerNumber, List<SkillCard> discards, SkillCardColor color) implements CharacterActionEvent {
}
