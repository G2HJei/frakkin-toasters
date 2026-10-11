package xyz.zlatanov.frakkintoasters.operation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import xyz.zlatanov.frakkintoasters.state.Game;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractOps {

    protected final Game game;
    //todo journal
}
