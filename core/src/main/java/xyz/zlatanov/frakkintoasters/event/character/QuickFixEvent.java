package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.QUICK_FIX;

// Cally's "Quick Fix": on her turn, once the cards of a skill check are revealed, she may discard one of them, before
// the abilities resolve and the strength is totaled.
@Source(ability = QUICK_FIX)
public record QuickFixEvent(int playerNumber, SkillCard card) implements PlayerEvent {
}
