package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// Damage vipers in space areas, in the "Reserves", or in either, e.g. "Damage 2 vipers in space areas."
public record DamageVipersEvent(int count, Area area) implements Event {

    public enum Area {
        SPACE,
        RESERVES,
        SPACE_OR_RESERVES
    }
}
