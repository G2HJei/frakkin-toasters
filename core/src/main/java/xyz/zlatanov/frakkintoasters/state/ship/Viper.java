package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;

@Data
@Accessors(fluent = true)
public class Viper implements Ship {
    private final ShipType type = VIPER;

}
