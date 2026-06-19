package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.board.Location;

public record MoveCivilianShipEvent(int civilianShipIt, Location location) implements LocationEvent {
    //todo
}
