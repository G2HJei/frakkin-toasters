package xyz.zlatanov.frakkintoasters.state;

public class Die {

    private final Randomness randomness;

    public Die() {
        this(new Randomness());
    }

    public Die(Randomness randomness) {
        this.randomness = randomness;
    }

    public int roll() {
        return randomness.nextInt(1, 9);
    }
}
