package xyz.zlatanov.frakkintoasters.event;

// A raider or a basestar attacks Galactica (rule 11.6).
public record AttackGalacticaEvent(int cylonShipId) implements Event {
}
