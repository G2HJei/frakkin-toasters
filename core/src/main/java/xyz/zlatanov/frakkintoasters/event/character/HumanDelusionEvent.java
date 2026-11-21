package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

// "Caprica" Six's "Human Delusion" (once per game): once the cards of a skill check are revealed, she adds any number
// of Skill Cards from her hand to the check, face up.
public record HumanDelusionEvent(int playerNumber, List<SkillCard> cards) implements PlayerEvent {
}
