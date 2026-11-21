package xyz.zlatanov.frakkintoasters.state.interrupt;

import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

/**
 * A character ability used in an interrupt window on a die roll, kept secret until the window closes.
 *
 * @param card   the Skill Card "Hotshot" discards, otherwise {@code null}
 * @param result the result "Longshot" chooses, otherwise {@code null}
 */
public record UsedRollAbility(int playerNumber, CharacterAbility ability, SkillCard card, Integer result) {
}
