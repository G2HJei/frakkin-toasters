package xyz.zlatanov.frakkintoasters.event;

// The attack on Galactica, after the die roll. With a raider: 8 = damaged. With a basestar: 4-8 = damaged.
public record AttackGalacticaResultEvent(int cylonShipId) implements Event {
}
