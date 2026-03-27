package xyz.zlatanov.frakkintoasters.ship;

import lombok.Getter;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.ship.ShipType.ASSAULT_RAPTOR;

@Getter
@Accessors(fluent = true)
public class AssaultRaptor implements Ship {

    private final ShipType type = ASSAULT_RAPTOR;
}
