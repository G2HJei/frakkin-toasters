package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.ViperActivation;

/**
 * One activation a card grants, of the player's own viper or an unmanned one, e.g. by "Combat Veteran". It is only ever
 * an option that {@link ViperActivationRoundEvent} built, never the answer to a decision.
 */
public record GrantedViperActivationEvent(int playerNumber, ViperActivation activation) implements PlayerEvent {
}
