package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

// The second half of "Treatment": the Skill Cards the Doc gives to the player he treated. Offered as options, so
// he can't change who gets them.
public record TreatmentGiveEvent(int playerNumber, int targetPlayerNumber, List<SkillCard> cards) implements PlayerEvent {
}
