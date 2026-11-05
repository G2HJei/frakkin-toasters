package xyz.zlatanov.frakkintoasters.journal.fact;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.ship.ShipZone;

/**
 * Facts about ships. Ships (and centurions) are named by their fixed ids.
 */
public sealed interface ShipFact extends Fact {

    record ShipMoved(int shipId, ShipZone from, ShipZone to, Location location) implements ShipFact {
    }

    /**
     * @param pilot the new pilot, or {@code null} when the fighter has no pilot anymore
     */
    record PilotChanged(int shipId, Character pilot) implements ShipFact {
    }

    record BasestarDamageAttached(int shipId, BasestarDamage card) implements ShipFact {
    }

    record BasestarDamageCleared(int shipId) implements ShipFact {
    }
}
