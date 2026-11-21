package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The player who chooses the vipers of a "damage vipers" effect damages this one. An assault raptor counts as a viper and
// is destroyed instead (rules 11.3.2.2 and 11.3.2.3).
public record DamageChosenViperEvent(int playerNumber, int humanFighterId) implements PlayerEvent {
}
