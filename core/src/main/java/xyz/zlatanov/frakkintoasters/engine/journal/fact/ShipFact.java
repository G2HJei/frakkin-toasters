package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.ship.ShipZone;

public sealed interface ShipFact extends Fact {

    record ShipMoved(int shipId, ShipZone from, ShipZone to, Location location) implements ShipFact {
    }

    record PilotChanged(int shipId, Character pilot) implements ShipFact {
    }

    record BasestarDamageAttached(int shipId, BasestarDamage card) implements ShipFact {
    }

    record BasestarDamageCleared(int shipId) implements ShipFact {
    }
}
