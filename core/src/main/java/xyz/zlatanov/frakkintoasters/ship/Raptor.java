package xyz.zlatanov.frakkintoasters.ship;

import lombok.Data;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.ship.ShipType.RAPTOR;

@Data
@Accessors(fluent = true)
public class Raptor implements Ship {
    private final ShipType type = RAPTOR;

}
