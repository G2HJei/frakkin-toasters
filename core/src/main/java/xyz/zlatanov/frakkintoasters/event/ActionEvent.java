package xyz.zlatanov.frakkintoasters.event;

/**
 * The acting player is not always the current player (e.g. the target of an "Executive Order" card gets a bonus
 * action), so it is carried by the event.
 */
public interface ActionEvent extends PlayerEvent {
}
