package xyz.zlatanov.frakkintoasters.fake;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Die;

public class FakeDie extends Die {

    private int nextRoll = 0;

    public void nextRoll(int next) {
        nextRoll = next;
    }

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
