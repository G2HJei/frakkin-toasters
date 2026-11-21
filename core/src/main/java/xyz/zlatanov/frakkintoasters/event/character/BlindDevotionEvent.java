package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

// The Chief's "Blind Devotion" (once per game): once the cards are in a skill check and before they are revealed, he
// names a skill type, and its cards count as strength 0 in that check.
public record BlindDevotionEvent(int playerNumber, SkillCardColor color) implements PlayerEvent {
}
