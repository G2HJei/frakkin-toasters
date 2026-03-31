package xyz.zlatanov.frakkintoasters.action;

import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;

public record MoveAction(int playerNumber, Location location) implements Action {


    @Override
    public void apply(Game game) {
        
    }
}
