package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.ASSAULT_RAPTOR;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class AssaultRaptor implements PilotableShip {

    private final int       id;
    private final ShipType  type = ASSAULT_RAPTOR;
    private       Character pilot;
}
