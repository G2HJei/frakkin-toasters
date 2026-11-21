package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

/**
 * One round of a viper's repeated activation, e.g. "Maximum Firepower"'s attacks. It is queued, never submitted, so the
 * viper and the count stay what the card decided. It offers the legal activations of the viper as concrete options,
 * each followed by the next round, or stopping.
 *
 * @param viperId         the viper, the player's own or an unmanned one
 * @param activationsLeft the activations left, this one included
 * @param attacksOnly     whether only attacks may be chosen
 */
public record ViperActivationRoundEvent(int playerNumber, int viperId, int activationsLeft, boolean attacksOnly)
        implements PlayerEvent {
}
