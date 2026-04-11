package xyz.zlatanov.frakkintoasters.state;

import java.util.Random;

public class Die {

    public int roll() {
        return new Random().nextInt(1, 8);
    }
}
