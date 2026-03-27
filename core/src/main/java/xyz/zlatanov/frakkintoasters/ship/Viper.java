package xyz.zlatanov.frakkintoasters.ship;

import lombok.Data;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.ship.ShipType.VIPER;

@Data
@Accessors(fluent = true)
public class Viper implements Ship {
    private final ShipType type = VIPER;

}
