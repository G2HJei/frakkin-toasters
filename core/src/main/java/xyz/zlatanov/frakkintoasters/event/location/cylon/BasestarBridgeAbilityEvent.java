package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Resolves one of the abilities of the "Basestar Bridge" for the player who activated it.
public record BasestarBridgeAbilityEvent(int playerNumber, BasestarBridgeAbility ability) implements PlayerEvent {
}
