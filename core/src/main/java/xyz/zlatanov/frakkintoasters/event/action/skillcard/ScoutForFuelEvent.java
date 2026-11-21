package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Scout for Fuel (SCOUTING_FOR_FUEL) - color: TACTICS, value: 6
// Action: risk a raptor and roll a die. On 4 or higher, gain 1 fuel. Otherwise destroy the raptor.
public record ScoutForFuelEvent(int playerNumber, SkillCard card) implements SkillCardEvent, ActionEvent {
}
