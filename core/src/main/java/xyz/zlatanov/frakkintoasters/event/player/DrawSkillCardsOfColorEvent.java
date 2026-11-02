package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

// The player draws Skill Cards of the given color, e.g. "the current player draws 2 Treachery Cards." Unlike
// DrawSkillCardsEvent there is no choice, and the player's skill set doesn't matter.
public record DrawSkillCardsOfColorEvent(int playerNumber, SkillCardColor color, int count) implements PlayerEvent {
}
