package xyz.zlatanov.frakkintoasters.event.action;

/**
 * Uses a "Movement:" ability printed on the player's character sheet, instead of moving in his Movement step (rule
 * 8.3.5). Each ability has one record in {@code action/character/}, named after it. {@link CharacterMovementEventProcessor}
 * runs it.
 */
public interface CharacterMovementEvent extends MovementAbilityEvent {
}
