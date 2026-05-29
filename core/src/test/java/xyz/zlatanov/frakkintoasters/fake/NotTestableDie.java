package xyz.zlatanov.frakkintoasters.fake;

import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

public class NotTestableDie extends FakeDie {
    @Override
    public int roll() {
        throw new FrakCallTheAdmiralException("Use FakeDie for test purposes");
    }
}
