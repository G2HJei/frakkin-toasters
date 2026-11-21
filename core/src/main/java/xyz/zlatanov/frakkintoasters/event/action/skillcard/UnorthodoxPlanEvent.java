package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Unorthodox Plan - color: TACTICS, values: 3, 4
// Action: take the action of "Command", "Armory", "Weapons Control" or "Communications", even if it is damaged.
//
// @param location the location whose action the player takes, from wherever he is
public record UnorthodoxPlanEvent(int playerNumber, SkillCard card, Location location) implements SkillCardEvent, ActionEvent {
}
