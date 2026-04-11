package xyz.zlatanov.frakkintoasters.event.player;

import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.List;

@Accessors(fluent = true)
public record LaunchViperEvent(int playerNumber, ShipType shipType, Location location) implements PlayerEvent {
    @Override
    public List<Followup> apply(Game game) {
        return List.of();
    }
}
