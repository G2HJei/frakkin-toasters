package xyz.zlatanov.frakkintoasters.event.player;

import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

@Accessors(fluent = true)
public record LaunchViperEvent(int playerNumber, ShipType shipType, Location location) implements PlayerEvent {
    @Override
    public void apply(Game game) {

    }
}
