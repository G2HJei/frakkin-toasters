package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.TREATMENT;

// Sherman "Doc" Cottle's "Treatment": he draws 2 Skill Cards (colors) from the skill set of another human, then
// gives that player 2 Skill Cards of his hand (TreatmentGiveEvent).
@Source(ability = TREATMENT)
public record TreatmentEvent(int playerNumber, int targetPlayerNumber, List<SkillCardColor> colors) implements CharacterActionEvent {
}
