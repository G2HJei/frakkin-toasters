package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.STAR_PLAYER;

// Samuel T. Anders' "Star Player": he discards Skill Cards and draws as many of one type of his skill set.
@Source(ability = STAR_PLAYER)
public record StarPlayerEvent(int playerNumber, List<SkillCard> discards, SkillCardColor color) implements CharacterActionEvent {
}
