package xyz.zlatanov.frakkintoasters.event;

/**
 * An answer the other players mustn't learn while it is pending, not even who is asked. A decision on a secret event
 * shows only to the player who answers it. Interrupt windows use it: asking everyone before and after every die roll
 * would make a Cylon attack unplayable, so only the holders of a card are asked.
 */
public interface SecretEvent extends Event {
}
