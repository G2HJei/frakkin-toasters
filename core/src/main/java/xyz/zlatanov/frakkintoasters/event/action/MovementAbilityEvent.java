package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

/**
 * A "Movement:" ability, used instead of moving in the player's own Movement step, at most one a turn (rule 8.3.5). It
 * can't be used when another effect lets him move, e.g. an "Executive Order".
 */
public interface MovementAbilityEvent extends PlayerEvent {
}
