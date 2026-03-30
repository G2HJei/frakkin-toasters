package xyz.zlatanov.frakkintoasters.action;

import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

public record MoveAction(int playerNumber, Location location) implements Action {

    @Override
    public List<Action> apply(Game game) {
        return List.of();
    }
}
