package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

@Getter
@Accessors(fluent = true)
public enum LocationsArea {
    GALACTICA(
            FTL_CONTROL,
            WEAPONS_CONTROL,
            COMMUNICATIONS,
            RESEARCH_LAB,
            ARMORY,
            COMMAND,
            ADMIRALS_QUARTERS,
            HANGAR_DECK,
            SICKBAY,
            BRIG),
    GALACTICA_SPACE( // in clockwise order
            GALACTICA_SPACE_1_OCLOCK,
            GALACTICA_SPACE_3_OCLOCK,
            GALACTICA_SPACE_5_OCLOCK,
            GALACTICA_SPACE_7_OCLOCK,
            GALACTICA_SPACE_9_OCLOCK,
            GALACTICA_SPACE_11_OCLOCK),
    COLONIAL_ONE(
            QUORUM_CHAMBER,
            PRESS_ROOM,
            PRESIDENTS_OFFICE,
            ADMINISTRATION),
    CYLON_LOCATIONS(
            BASESTAR_BRIDGE,
            CAPRICA,
            CYLON_FLEET,
            HUMAN_FLEET,
            RESURRECTION_SHIP),
    CYLON_FLEET_SPACE(
            CYLON_FLEET_SPACE_1,
            CYLON_FLEET_SPACE_2,
            CYLON_FLEET_SPACE_3,
            CYLON_FLEET_SPACE_4,
            CYLON_FLEET_SPACE_5_6,
            CYLON_FLEET_SPACE_7_8),
    PEGASUS(
            PEGASUS_CIC,
            AIRLOCK,
            MAIN_BATTERIES,
            ENGINE_ROOM);

    private final List<Location> locations;

    public static final List<Location> SPACE_LOCATIONS     = Stream.of(GALACTICA_SPACE.locations(), CYLON_FLEET_SPACE.locations()).flatMap(List::stream).toList();
    public static final List<Location> HAZARDOUS_LOCATIONS = List.of(SICKBAY, BRIG, RESURRECTION_SHIP);

    public static final Map<Location, Map<Location, Integer>> DISTANCE_LOOKUP_TABLE = Map.of(
            GALACTICA_SPACE_3_OCLOCK, Map.of(
                    GALACTICA_SPACE_5_OCLOCK, 1,
                    GALACTICA_SPACE_7_OCLOCK, 2,
                    GALACTICA_SPACE_9_OCLOCK, 3,
                    GALACTICA_SPACE_11_OCLOCK, 2,
                    GALACTICA_SPACE_1_OCLOCK, 1),
            GALACTICA_SPACE_5_OCLOCK, Map.of(
                    GALACTICA_SPACE_7_OCLOCK, 1,
                    GALACTICA_SPACE_9_OCLOCK, 2,
                    GALACTICA_SPACE_11_OCLOCK, 3,
                    GALACTICA_SPACE_1_OCLOCK, 2,
                    GALACTICA_SPACE_3_OCLOCK, 1),
            GALACTICA_SPACE_7_OCLOCK, Map.of(
                    GALACTICA_SPACE_9_OCLOCK, 1,
                    GALACTICA_SPACE_11_OCLOCK, 2,
                    GALACTICA_SPACE_1_OCLOCK, 3,
                    GALACTICA_SPACE_3_OCLOCK, 2,
                    GALACTICA_SPACE_5_OCLOCK, 1),
            GALACTICA_SPACE_9_OCLOCK, Map.of(
                    GALACTICA_SPACE_11_OCLOCK, 1,
                    GALACTICA_SPACE_1_OCLOCK, 2,
                    GALACTICA_SPACE_3_OCLOCK, 3,
                    GALACTICA_SPACE_5_OCLOCK, 2,
                    GALACTICA_SPACE_7_OCLOCK, 1),
            GALACTICA_SPACE_11_OCLOCK, Map.of(
                    GALACTICA_SPACE_1_OCLOCK, 1,
                    GALACTICA_SPACE_3_OCLOCK, 2,
                    GALACTICA_SPACE_5_OCLOCK, 3,
                    GALACTICA_SPACE_7_OCLOCK, 2,
                    GALACTICA_SPACE_9_OCLOCK, 1),
            GALACTICA_SPACE_1_OCLOCK, Map.of(
                    GALACTICA_SPACE_3_OCLOCK, 1,
                    GALACTICA_SPACE_5_OCLOCK, 2,
                    GALACTICA_SPACE_7_OCLOCK, 3,
                    GALACTICA_SPACE_9_OCLOCK, 2,
                    GALACTICA_SPACE_11_OCLOCK, 1)
    );

    LocationsArea(Location... locations) {
        this.locations = List.of(locations);
    }

}
