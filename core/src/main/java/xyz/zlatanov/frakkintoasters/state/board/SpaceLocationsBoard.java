package xyz.zlatanov.frakkintoasters.state.board;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface SpaceLocationsBoard {
    Map<Ship, Location> shipsInSpace();

    default Location locate(Ship ship) {
        val location = shipsInSpace().get(ship);
        assert location != null;
        return location;
    }

    default <T extends Ship> List<T> shipsIn(Location location, Class<T> shipClass) {
        return shipsInSpace().entrySet()
                .stream()
                .filter(e -> e.getValue() == location)
                .map(Map.Entry::getKey)
                .filter(s -> shipClass.equals(s.getClass()))
                .map(shipClass::cast)
                .toList();
    }

    default List<Ship> shipsIn(Location location) {
        return shipsInSpace().entrySet()
                .stream()
                .filter(e -> e.getValue() == location)
                .map(Map.Entry::getKey)
                .toList();
    }

    default <T extends Ship> T shipInSpace(int shipId, Class<T> shipClass) {
        return shipsInSpace(shipClass).stream()
                .filter(r -> r.id() == shipId)
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
    }

    default <T extends Ship> List<T> shipsInSpace(Class<T> shipClass) {
        return shipsInSpace().keySet()
                .stream()
                .filter(s -> shipClass.equals(s.getClass()))
                .map(shipClass::cast)
                .toList();
    }

    default List<HumanFighter> humanFightersIn(Location location) {
        return shipsIn(location).stream()
                .filter(HumanFighter.class::isInstance)
                .map(HumanFighter.class::cast)
                .toList();
    }

    default SpaceLocationsBoard place(Location in, Ship... ships) {
        return place(in, Arrays.stream(ships).toList());
    }

    default SpaceLocationsBoard place(Location in, List<Ship> ships) {
        ships.forEach(s -> place(in, s));
        return this;
    }

    default SpaceLocationsBoard place(Location in, Ship ship) {
        assert in.isSpaceLocation();
        shipsInSpace().put(ship, in);
        return this;
    }

    default SpaceLocationsBoard remove(Ship ship) {
        assert shipsInSpace().containsKey(ship);
        shipsInSpace().remove(ship);
        return this;
    }
}
