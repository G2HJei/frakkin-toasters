package xyz.zlatanov.frakkintoasters.event;

/**
 * An answer the other players mustn't learn while it is pending, not even who is asked (decision 8 in
 * {@code engine-plan.md}). A decision on a secret event shows only to the player who answers it.
 */
public interface SecretEvent extends Event {
}
