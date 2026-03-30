package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

@Data
@Accessors(fluent = true)
public class Basestar implements Ship {
    private final ShipType type = BASESTAR;

}
