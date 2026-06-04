package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.experimental.Accessors;

@Accessors(fluent = true)
public record CivilianShip(int id, int fuelCost, int moraleCost, int populationCost) implements Ship {
}
