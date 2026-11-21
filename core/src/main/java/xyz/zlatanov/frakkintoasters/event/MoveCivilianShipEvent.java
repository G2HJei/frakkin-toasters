package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.board.Location;

// "Communications": a civilian ship moves to an adjacent space area. It is an option the action builds.
public record MoveCivilianShipEvent(int civilianShipId, Location location) implements Event {
}
