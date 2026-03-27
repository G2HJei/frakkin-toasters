package xyz.zlatanov.frakkintoasters.ship;

import lombok.Data;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.ship.ShipType.BASESTAR;

@Data
@Accessors(fluent = true)
public class Basestar implements Ship {
    private final ShipType type = BASESTAR;

}
