package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class Raider implements Ship {
    private final int      id;
    private final ShipType type = RAIDER;

}
