package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Quick Thinking - color: TACTICS, values: 0
// Skill Check: The current player may choose 1 card with a strength of 3 or less (not a "Quick Thinking" Card) to
// remove from this check and add to his hand.
public record QuickThinkingEvent(SkillCard cardToRemove) implements Event {
}
