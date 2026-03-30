package xyz.zlatanov.frakkintoasters.action;

import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.List;

public record LaunchViperAction(int playerNumber, ShipType shipType, Location location) implements Action {
    @Override
    public List<Action> apply(Game game) {
        return List.of();
    }
}
