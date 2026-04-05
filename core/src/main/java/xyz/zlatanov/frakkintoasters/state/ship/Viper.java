package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;

@Data
@Accessors(fluent = true)
public class Viper implements Ship, Pilotable {
    private final ShipType  type = VIPER;
    private       Character pilot;
}
