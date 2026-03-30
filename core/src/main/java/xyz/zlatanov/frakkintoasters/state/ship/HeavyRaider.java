package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;

@Data
@Accessors(fluent = true)
public class HeavyRaider implements Ship {
    private final ShipType type = HEAVY_RAIDER;

}
