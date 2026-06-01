package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

public record PlaceCrisisCardOnBottomEvent(int playerNumber, CrisisCard card) implements PlayerEvent {
}
