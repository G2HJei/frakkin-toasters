package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

/**
 * One round of a viper's repeated activation, e.g. "Maximum Firepower"'s attacks. It is queued, never submitted, so the
 * viper and the count stay what the card decided.
 *
 * @param activationsLeft this one included
 */
public record ViperActivationRoundEvent(int playerNumber, int viperId, int activationsLeft, boolean attacksOnly)
        implements PlayerEvent {
}
