package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Negotiation - color: POLITICS, value: 5
// Action: place a basestar in front of Galactica and draw 1 Politics Card. No Cylon ship launches or activates for the
// rest of this turn.
public record NegotiationEvent(int playerNumber, SkillCard card) implements SkillCardEvent, ActionEvent {
}
