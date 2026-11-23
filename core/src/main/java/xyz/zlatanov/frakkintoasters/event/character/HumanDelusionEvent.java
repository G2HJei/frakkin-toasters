package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.HUMAN_DELUSION;

// "Caprica" Six's "Human Delusion" (once per game): once the cards of a skill check are revealed, she adds any number
// of Skill Cards from her hand to the check, face up.
@Source(ability = HUMAN_DELUSION)
public record HumanDelusionEvent(int playerNumber, List<SkillCard> cards) implements PlayerEvent {
}
