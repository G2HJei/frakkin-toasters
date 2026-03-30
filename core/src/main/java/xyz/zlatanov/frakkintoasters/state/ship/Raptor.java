package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAPTOR;

@Data
@Accessors(fluent = true)
public class Raptor implements Ship {
    private final ShipType type = RAPTOR;

}
