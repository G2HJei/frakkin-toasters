package xyz.zlatanov.frakkintoasters.event.deck;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

// The whole Destiny deck is discarded and a new one is built from that many cards of the color, e.g. 6 Treachery for
// "Psychological Warfare".
public record ReplaceDestinyDeckEvent(SkillCardColor color, int count) implements Event {
}
