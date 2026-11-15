package xyz.zlatanov.frakkintoasters.event.interrupt;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.SecretEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// A player's answer to an interrupt window: a card for the window, face down, or no card to pass.
public record PlayInterruptCardEvent(int playerNumber, SkillCard card, Integer calculationsModifier)
        implements PlayerEvent, SecretEvent {

    public static PlayInterruptCardEvent pass(int playerNumber) {
        return new PlayInterruptCardEvent(playerNumber, null, null);
    }

    public static PlayInterruptCardEvent play(int playerNumber, SkillCard card) {
        return new PlayInterruptCardEvent(playerNumber, card, null);
    }
}
