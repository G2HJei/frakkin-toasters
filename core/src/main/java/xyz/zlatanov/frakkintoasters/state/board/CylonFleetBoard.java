package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.track.Pursuit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.track.Pursuit.START;

@Getter
@Accessors(fluent = true)
public class CylonFleetBoard extends Board {

    public static final List<Location>          SPACE_AREAS           = List.of(
            CYLON_FLEET_SPACE_1, CYLON_FLEET_SPACE_2, CYLON_FLEET_SPACE_3,
            CYLON_FLEET_SPACE_4, CYLON_FLEET_SPACE_5_6, CYLON_FLEET_SPACE_7_8);
    public static final Map<Location, Location> MOVE_TO_GALACTICA_MAP = Map.of(
            CYLON_FLEET_SPACE_1, GALACTICA_SPACE_2_OCLOCK,
            CYLON_FLEET_SPACE_2, GALACTICA_SPACE_4_OCLOCK,
            CYLON_FLEET_SPACE_3, GALACTICA_SPACE_6_OCLOCK,
            CYLON_FLEET_SPACE_4, GALACTICA_SPACE_8_OCLOCK,
            CYLON_FLEET_SPACE_5_6, GALACTICA_SPACE_10_OCLOCK,
            CYLON_FLEET_SPACE_7_8, GALACTICA_SPACE_12_OCLOCK);

    private       Pursuit             pursuitTrack = START;
    private final Map<Ship, Location> shipsInSpace = new HashMap<>();

    public CylonFleetBoard() {
        super(Set.of(BASESTAR_BRIDGE,
                CYLON_FLEET_SPACE_1,
                CYLON_FLEET_SPACE_2,
                CYLON_FLEET_SPACE_3,
                CYLON_FLEET_SPACE_4,
                CYLON_FLEET_SPACE_5_6,
                CYLON_FLEET_SPACE_7_8));
    }

    public static Location spaceFromRoll(int roll) {
        return switch (roll) {
            case 1 -> CYLON_FLEET_SPACE_1;
            case 2 -> CYLON_FLEET_SPACE_2;
            case 3 -> CYLON_FLEET_SPACE_3;
            case 4 -> CYLON_FLEET_SPACE_4;
            case 5, 6 -> CYLON_FLEET_SPACE_5_6;
            case 7, 8 -> CYLON_FLEET_SPACE_7_8;
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    public CylonFleetBoard place(Location in, Ship ship) {
        assert SPACE_AREAS.contains(in);
        shipsInSpace.put(ship, in);
        return this;
    }

    public CylonFleetBoard remove(Ship ship) {
        shipsInSpace.remove(ship);
        return this;
    }

    public <T extends Ship> List<T> shipsIn(Location location, Class<T> shipClass) {
        return shipsInSpace.entrySet().stream()
                .filter(e -> e.getValue() == location)
                .map(Map.Entry::getKey)
                .filter(shipClass::isInstance)
                .map(shipClass::cast)
                .toList();
    }

    public void advancePursuit() {
        val current = pursuitTrack.ordinal();
        val autoAttack = Pursuit.values().length - 1;
        val next = current == autoAttack ? 0 : current + 1;
        pursuitTrack = Pursuit.values()[next];
    }
}
