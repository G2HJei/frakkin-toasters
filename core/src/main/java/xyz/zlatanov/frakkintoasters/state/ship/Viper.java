package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class Viper implements PilotableShip {
    private final int       id;
    private final ShipType  type = VIPER;
    private       Character pilot;
}
