package xyz.zlatanov.frakkintoasters.operation;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.frakkintoasters.state.Game;

@RequiredArgsConstructor
public abstract class GameOperation {

    private final Game game;
    //todo journal
}
