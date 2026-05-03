package xyz.zlatanov.frakkintoasters.event;

public record AttackViperEvent(int raiderId, int pilotableShipId) implements Event {
}
