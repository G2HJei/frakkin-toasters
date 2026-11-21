package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

/**
 * Plays a Skill Card from the hand for the ability printed on it, its "Action:" or its "Movement:". The interface of
 * its timing, e.g. {@link xyz.zlatanov.frakkintoasters.event.ActionEvent}, makes it answer only the decisions of that
 * timing. {@link SkillCardEventProcessor} runs it.
 */
public interface SkillCardEvent extends PlayerEvent {

    SkillCard card();
}
