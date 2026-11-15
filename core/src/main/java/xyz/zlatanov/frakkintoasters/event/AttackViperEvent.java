package xyz.zlatanov.frakkintoasters.event;

// A raider attacks a viper (or an assault raptor) in its space area (rule 11.5.1).
public record AttackViperEvent(int raiderId, int humanFighterId) implements Event {
}
