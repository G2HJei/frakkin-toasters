package xyz.zlatanov.frakkintoasters.state.ship;

import java.util.Comparator;

public interface Ship {

    // Typed on Ship, so contains() on a sorted set of Vipers answers false for another ship instead of failing the cast.
    Comparator<Ship> BY_ID = Comparator.comparingInt(Ship::id);

    int id();
}
