package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

/**
 * Uses an "Action:" ability printed on the player's character sheet. Each ability has one record in
 * {@code action/character/}, named after it, holding only the player and the ability's choices.
 * {@link CharacterActionEventProcessor} runs it.
 */
public interface CharacterActionEvent extends ActionEvent {
}
