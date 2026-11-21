package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Event;

// What the die decides after "Civilian Self Defense": the civilian ship is destroyed on 2 or less. Then the card is
// discarded.
public record CivilianSelfDefenseResultEvent(int civilianShipId) implements Event {
}
