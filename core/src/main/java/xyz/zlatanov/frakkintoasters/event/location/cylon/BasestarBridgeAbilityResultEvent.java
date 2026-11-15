package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The ability of the "Basestar Bridge" that rolls a die, after the roll.
public record BasestarBridgeAbilityResultEvent(int playerNumber, BasestarBridgeAbility ability) implements PlayerEvent {
}
