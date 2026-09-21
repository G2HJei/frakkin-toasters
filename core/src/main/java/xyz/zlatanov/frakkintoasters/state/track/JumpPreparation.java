package xyz.zlatanov.frakkintoasters.state.track;

import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

public enum JumpPreparation {
    START, POSITION_1, POSITION_2, MINUS_3, MINUS_1;

    public int lostPopulation() {
        return switch (this) {
            case MINUS_3 -> 3;
            case MINUS_1 -> 1;
            default -> throw new FrakCallTheAdmiralException();
        };
    }
}
