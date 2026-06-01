package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

public record HumanFleetInfiltrateEvent(int playerNumber, Location galacticaLocation) implements PlayerEvent {
}
