package xyz.zlatanov.frakkintoasters.ship;

import lombok.Data;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.ship.ShipType.RAIDER;

@Data
@Accessors(fluent = true)
public class Raider implements Ship {
    private final ShipType type = RAIDER;

}
