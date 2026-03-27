package xyz.zlatanov.frakkintoasters.ship;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.ship.ShipType.CIVILIAN;

@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
public class CivilianShip implements Ship {
    private final ShipType type = CIVILIAN;
    private final int      fuelCost;
    private final int      moraleCost;
    private final int      populationCost;
}
