package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Declare Emergency - color: LEADERSHIP, values: 3, 4, 5
// Play after strength is totaled in a skill check to reduce its difficulty by 2. Limit of 1 "Declare Emergency" card
// used per skill check.
public record DeclareEmergencyEvent(int playerNumber, SkillCard declareEmergencyCard) implements PlayerEvent {
}
