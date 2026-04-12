package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;

public record DistributeBasestarDamageEvent(int playerNumber, BasestarDamage damage) implements PlayerEvent {
}
