package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

@Data
@Accessors(fluent = true)
public class Raider implements Ship {
    private final ShipType type = RAIDER;

}
