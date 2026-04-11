package xyz.zlatanov.frakkintoasters.fake;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Die;

public class FakeDie extends Die {

    public int nextRoll = 0;

    @Override
    public int roll() {
        if (nextRoll >= 1 && nextRoll <= 8) {
            val toReturn = nextRoll;
            nextRoll = 0;
            return toReturn;
        }
        return super.roll();
    }
}
