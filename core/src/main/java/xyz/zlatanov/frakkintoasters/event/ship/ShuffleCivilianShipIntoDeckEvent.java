package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// A civilian ship in space is shuffled back into the civilian ship deck, e.g. by "Scavenging for Parts". Whoever looked
// at it forgets it.
public record ShuffleCivilianShipIntoDeckEvent(int civilianShipId) implements Event {
}
