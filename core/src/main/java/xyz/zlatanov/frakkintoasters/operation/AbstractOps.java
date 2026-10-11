package xyz.zlatanov.frakkintoasters.operation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractOps {

    protected final Game game;
    //todo journal

    protected GalacticaBoard galacticaBoard() {
        return game.boards().galactica();
    }

    protected CylonFleetBoard cylonFleetBoard() {
        return game.boards().cylonFleet();
    }
}
