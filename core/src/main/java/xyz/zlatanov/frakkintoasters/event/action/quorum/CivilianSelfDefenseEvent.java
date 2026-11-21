package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

// Civilian Self Defense (Quorum Card)
// Action: pick a civilian ship. Destroy 3 raiders or 1 heavy raider in its space area, then roll a die. On 2 or less
// the civilian ship is destroyed. Then the card is discarded.
//
// @param target RAIDER or HEAVY_RAIDER: ships of a type are alike, so the type is enough
public record CivilianSelfDefenseEvent(int playerNumber, int civilianShipId, ShipType target) implements QuorumCardEvent {
}
