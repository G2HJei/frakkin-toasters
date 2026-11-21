package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.action.MovementAbilityEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Preventative Policy - color: POLITICS, values: 3, 4, 5
// Movement: the player names a resource. Its next loss this turn is reduced by 1.
public record PreventivePolicyEvent(int playerNumber, SkillCard card, Resource resource)
        implements SkillCardEvent, MovementAbilityEvent {
}
