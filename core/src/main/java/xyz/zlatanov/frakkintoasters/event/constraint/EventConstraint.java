package xyz.zlatanov.frakkintoasters.event.constraint;

import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

public enum EventConstraint {
    DRAW_EXACTLY_1,
    DRAW_EXACTLY_2,
    DRAW_EXACTLY_3,
    DRAW_EXACTLY_4,
    DRAW_EXACTLY_5,
    DRAW_EXACTLY_1_OF_ANY_COLOR, // "Accept Prophecy": 1 Skill Card from any deck
    DRAW_EXACTLY_2_OF_ANY_COLOR, // "2 Skill Cards (they may be from outside his skill set)"
    DISCARD_EXACTLY_1,
    DISCARD_EXACTLY_2,
    DISCARD_EXACTLY_3,
    DISCARD_EXACTLY_4,
    DISCARD_EXACTLY_5,
    DISCARD_DOWN_TO_HAND_LIMIT,
    DISCARD_DOWN_TO_1, // "Deceitful": Romo Lampkin keeps 1 card of the hand a Crisis Card makes him discard
    DISCARD_DOWN_TO_3, // a Cylon who reveals keeps 3 Skill Cards (rule 6.1.1)
    MUST_LAUNCH_IN_VIPER,
    ANYWHERE_EVEN_IF_DAMAGED, // "Unorthodox Plan": a location's action from elsewhere, even when it is damaged
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

    public static EventConstraint discardDownTo(int count) {
        return switch (count) {
            case 1 -> DISCARD_DOWN_TO_1;
            case 3 -> DISCARD_DOWN_TO_3;
            default -> throw new FrakCallTheAdmiralException("No constraint to discard down to " + count);
        };
    }
}
