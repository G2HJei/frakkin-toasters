package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

/**
 * Plays a Mutiny Card as an action (rule 5.2.3.1). Each card has one record in {@code action/mutiny/}: the record is the
 * card, so it holds only the player and the card's choices. {@link MutinyCardEventProcessor} runs it.
 */
public interface MutinyCardEvent extends ActionEvent {
}
