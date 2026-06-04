package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.track.Pursuit;

import java.util.*;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.CYLON_FLEET_SPACE;
import static xyz.zlatanov.frakkintoasters.state.track.Pursuit.START;

@Getter
@Accessors(fluent = true)
public class CylonFleetBoard implements Board, SpaceLocationsBoard {

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

    private       Pursuit                  pursuitTrack     = START;
    private final Map<Character, Location> characters       = new HashMap<>();
    private final Set<Location>            locations        = new HashSet<>(Stream.of(CYLON_FLEET_SPACE.locations(), List.of(BASESTAR_BRIDGE)).flatMap(Collection::stream).toList());
    private final Set<Location>            damagedLocations = new HashSet<>();
    private final Map<Ship, Location>      shipsInSpace     = new HashMap<>();

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

    @Override
    public List<HumanFighter> humanFightersIn(Location location) {
        throw new UnsupportedOperationException();
    }

    public void advancePursuit() {
        val current = pursuitTrack.ordinal();
        val autoAttack = Pursuit.values().length - 1;
        val next = current == autoAttack ? 0 : current + 1;
        pursuitTrack = Pursuit.values()[next];
    }
}
