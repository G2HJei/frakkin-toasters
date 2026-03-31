package xyz.zlatanov.frakkintoasters.action;

import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

public record LaunchViperAction(int playerNumber, ShipType shipType, Location location) implements Action {

    @Override
    public void apply(Game game) {
        
    }
}
