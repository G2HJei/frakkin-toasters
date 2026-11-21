package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

/**
 * Plays a Quorum Card, as an action (rule 8.4) or through the Quorum Chamber. Each card has one record in
 * {@code action/quorum/}: the record is the card, so it holds only the player and the card's choices.
 * {@link QuorumCardEventProcessor} runs it.
 */
public interface QuorumCardEvent extends ActionEvent {
}
