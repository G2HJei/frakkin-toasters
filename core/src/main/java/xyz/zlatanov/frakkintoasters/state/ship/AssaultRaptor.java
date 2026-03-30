package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Getter;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.ASSAULT_RAPTOR;

@Getter
@Accessors(fluent = true)
public class AssaultRaptor implements Ship {

    private final ShipType type = ASSAULT_RAPTOR;
}
