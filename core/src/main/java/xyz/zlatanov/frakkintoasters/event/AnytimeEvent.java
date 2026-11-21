package xyz.zlatanov.frakkintoasters.event;

/**
 * Something a player may do at any pause, without being asked, e.g. reveal a Motive Card "any time its requirements
 * are currently being met" (rule 6.2.2.1). The engine runs it ahead of the pending input, which stays pending, and never
 * takes one as an answer to a decision.
 */
public interface AnytimeEvent extends PlayerEvent {
}
