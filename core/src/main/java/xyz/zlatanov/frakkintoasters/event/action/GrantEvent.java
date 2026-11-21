package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

/**
 * Lets the player act now, e.g. the "1 more action" of the "Hangar Deck". It is queued, never submitted. When it runs,
 * the player becomes the acting player, and his choice is offered as the game stands then.
 *
 * @param grant     what he may do
 * @param grantedBy what lets him, whose rules apply
 */
public record GrantEvent(int playerNumber, Grant grant, GrantedBy grantedBy) implements PlayerEvent {

    public enum Grant {
        ACTION
    }

    public enum GrantedBy {
        HANGAR_DECK
    }
}
