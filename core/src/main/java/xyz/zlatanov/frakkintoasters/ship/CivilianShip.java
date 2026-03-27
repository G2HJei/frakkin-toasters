package xyz.zlatanov.frakkintoasters.ship;

import lombok.Data;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.ship.ShipType.CIVILIAN;

@Data
@Accessors(fluent = true)
public class CivilianShip implements Ship {
    private final ShipType type = CIVILIAN;
}
