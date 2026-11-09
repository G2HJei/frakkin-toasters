package xyz.zlatanov.frakkintoasters.event.constraint;

import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

public enum EventConstraint {
    DRAW_EXACTLY_1,
    DRAW_EXACTLY_2,
    DRAW_EXACTLY_3,
    DRAW_EXACTLY_4,
    DRAW_EXACTLY_5,
    DISCARD_EXACTLY_1,
    DISCARD_EXACTLY_2,
    DISCARD_EXACTLY_3,
    DISCARD_EXACTLY_4,
    DISCARD_EXACTLY_5,
    DISCARD_DOWN_TO_HAND_LIMIT,
    MUST_LAUNCH_IN_VIPER,
    DESTINATION_GALACTICA;

    public static EventConstraint drawExactly(int count) {
        return switch (count) {
            case 1 -> DRAW_EXACTLY_1;
            case 2 -> DRAW_EXACTLY_2;
            case 3 -> DRAW_EXACTLY_3;
            case 4 -> DRAW_EXACTLY_4;
            case 5 -> DRAW_EXACTLY_5;
            default -> throw new FrakCallTheAdmiralException("No constraint to draw " + count);
        };
    }

    public static EventConstraint discardExactly(int count) {
        return switch (count) {
            case 1 -> DISCARD_EXACTLY_1;
            case 2 -> DISCARD_EXACTLY_2;
            case 3 -> DISCARD_EXACTLY_3;
            case 4 -> DISCARD_EXACTLY_4;
            case 5 -> DISCARD_EXACTLY_5;
            default -> throw new FrakCallTheAdmiralException("No constraint to discard " + count);
        };
    }
}
