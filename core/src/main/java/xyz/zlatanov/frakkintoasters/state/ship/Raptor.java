package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAPTOR;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class Raptor implements Ship {
    private final int      id;
    private final ShipType type = RAPTOR;

}
