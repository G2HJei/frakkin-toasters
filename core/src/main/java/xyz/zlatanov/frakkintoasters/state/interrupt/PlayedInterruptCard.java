package xyz.zlatanov.frakkintoasters.state.interrupt;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

/**
 * A card played into an interrupt window, face down until the window closes.
 *
 * @param calculationsModifier +1 or -1 for "Calculations", which adds or subtracts 1, otherwise {@code null}
 */
public record PlayedInterruptCard(int playerNumber, SkillCard card, Integer calculationsModifier) {
}
