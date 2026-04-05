package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.ASSAULT_RAPTOR;

@Data
@Accessors(fluent = true)
public class AssaultRaptor implements Ship, Pilotable {

    private final ShipType  type = ASSAULT_RAPTOR;
    private       Character pilot;
}
