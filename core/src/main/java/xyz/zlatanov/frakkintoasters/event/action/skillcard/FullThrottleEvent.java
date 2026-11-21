package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.MovementAbilityEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Full Throttle - color: PILOTING, values: 1, 2
// Movement or Action: only while piloting a viper. It may move to any space area, then it may attack 1 Cylon ship in
// its area, even if it didn't move.
//
// @param destination the space area, or null to stay
public record FullThrottleEvent(int playerNumber, SkillCard card, Location destination)
        implements SkillCardEvent, ActionEvent, MovementAbilityEvent {
}
