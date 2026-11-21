package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ViperActivation;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Combat Veteran - color: PILOTING, values: 3, 4
// Action: choose 1 unmanned viper, in the "Reserves" or in a space area, and activate it up to 3 times.
//
// @param first the first activation, which names the viper: a launch from the "Reserves" or one of a viper in space
public record CombatVeteranEvent(int playerNumber, SkillCard card, ViperActivation first)
        implements SkillCardEvent, ActionEvent {
}
