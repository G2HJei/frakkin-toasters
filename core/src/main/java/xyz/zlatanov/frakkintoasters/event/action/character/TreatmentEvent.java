package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.List;

// Sherman "Doc" Cottle's "Treatment": he draws 2 Skill Cards (colors) from the skill set of another human, then
// gives that player 2 Skill Cards of his hand (TreatmentGiveEvent).
public record TreatmentEvent(int playerNumber, int targetPlayerNumber, List<SkillCardColor> colors) implements CharacterActionEvent {
}
