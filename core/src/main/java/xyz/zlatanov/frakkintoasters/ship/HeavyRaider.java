package xyz.zlatanov.frakkintoasters.ship;

import lombok.Data;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.ship.ShipType.HEAVY_RAIDER;

@Data
@Accessors(fluent = true)
public class HeavyRaider implements Ship {
    private final ShipType type = HEAVY_RAIDER;

}
