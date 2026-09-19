package xyz.zlatanov.frakkintoasters.event;

public record AttackBasestarEvent(Attacker attacker, int basestarId) implements Event {
    //todo

    public static enum Attacker {
        VIPER, ASSAULT_RAPTOR, GALACTICA
    }
}
