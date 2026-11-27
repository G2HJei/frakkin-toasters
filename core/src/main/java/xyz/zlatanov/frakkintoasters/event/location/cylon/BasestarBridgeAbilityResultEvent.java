package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record BasestarBridgeAbilityResultEvent(int playerNumber, BasestarBridgeAbility ability) implements PlayerEvent {
}
