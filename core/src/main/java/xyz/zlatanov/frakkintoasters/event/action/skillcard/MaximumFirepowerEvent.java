package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Maximum Firepower - color: PILOTING, values: 3, 4, 5
// Action: only while piloting a viper. It attacks up to 4 times.
public record MaximumFirepowerEvent(int playerNumber, SkillCard card) implements SkillCardEvent, ActionEvent {
}
