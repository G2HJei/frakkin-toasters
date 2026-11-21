package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.AnywhereLocationEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.WEAPONS_CONTROL;

// Weapons Control (Galactica location)
// Action: Attack 1 Cylon ship with Galactica.
public record WeaponsControlEvent(int playerNumber, int cylonShipId, boolean anywhere) implements AnywhereLocationEvent {

    public WeaponsControlEvent(int playerNumber, int cylonShipId) {
        this(playerNumber, cylonShipId, false);
    }

    @Override
    public Location location() {
        return WEAPONS_CONTROL;
    }

    @Override
    public WeaponsControlEvent withConstraints(List<EventConstraint> constraints) {
        return new WeaponsControlEvent(playerNumber, cylonShipId, AnywhereLocationEvent.anywhere(constraints));
    }
}
