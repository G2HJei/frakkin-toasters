package xyz.zlatanov.frakkintoasters.state.ship;

/**
 * Every place a ship (or a centurion) can be in.
 */
public enum ShipZone {
    CYLON_SUPPLY,
    CENTURION_SUPPLY,
    CIVILIAN_DECK,
    RESERVES,
    HUMAN_SUPPLY, // the assault raptors the humans haven't built yet
    DAMAGED,
    GALACTICA_SPACE,
    CYLON_FLEET_SPACE,
    BOARDING_PARTY,
    REMOVED
}
