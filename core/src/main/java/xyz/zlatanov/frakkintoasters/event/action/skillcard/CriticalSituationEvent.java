package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.action.MovementAbilityEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Critical Situation - color: TACTICS, values: 3, 4, 5
// Movement: the player takes 1 action. Only 1 "Critical Situation" or "Executive Order" may be used per turn.
public record CriticalSituationEvent(int playerNumber, SkillCard card) implements SkillCardEvent, MovementAbilityEvent {
}
