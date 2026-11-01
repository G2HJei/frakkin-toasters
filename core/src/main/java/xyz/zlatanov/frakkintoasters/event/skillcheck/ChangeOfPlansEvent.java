package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Change of Plans - color: LEADERSHIP, values: 5
// Play this card after a skill check is passed. Instead of resolving the "pass" result, each human player draws 2
// Skill Cards.
public record ChangeOfPlansEvent(int playerNumber, SkillCard changeOfPlansCard) implements PlayerEvent {
}
