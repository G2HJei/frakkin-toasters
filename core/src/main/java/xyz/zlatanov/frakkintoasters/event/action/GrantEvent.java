package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

/**
 * Lets the player act now, e.g. the target of an "Executive Order". It is queued, never submitted. When it runs, the
 * player becomes the acting player, and his choice is offered as the game stands then, so grants can nest.
 *
 * @param grant     what he may do
 * @param grantedBy what lets him, whose rules apply
 */
public record GrantEvent(int playerNumber, Grant grant, GrantedBy grantedBy) implements PlayerEvent {

    public enum Grant {
        ACTION,
        MOVE,
        MOVE_OR_ACTION
    }

    public enum GrantedBy {
        HANGAR_DECK,
        EXECUTIVE_ORDER,
        STATE_OF_EMERGENCY,
        CAG_TITLE,
        EXPERT_PILOT,
        METICULOUS,
        AGGRESSIVE_TACTICS,
        MAINTENANCE_ENGINEER,
        ALERT_VIPER_PILOT
    }
}
