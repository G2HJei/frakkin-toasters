package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

// "Keep this card in play." The lasting effect printed on the card applies for the rest of the game.
public record KeepCrisisCardInPlayEvent(SkillCheckType skillCheckType) implements Event {
}
