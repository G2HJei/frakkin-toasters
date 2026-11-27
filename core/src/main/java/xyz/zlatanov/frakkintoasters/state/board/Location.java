package xyz.zlatanov.frakkintoasters.state.board;

import java.util.List;
import java.util.Map;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.*;

public enum Location {
    FTL_CONTROL,
    WEAPONS_CONTROL,
    COMMUNICATIONS,
    RESEARCH_LAB,
    ARMORY,
    COMMAND,
    ADMIRALS_QUARTERS,
    HANGAR_DECK,
    SICKBAY,
    BRIG,

    QUORUM_CHAMBER,
    PRESS_ROOM,
    PRESIDENTS_OFFICE,
    ADMINISTRATION,

    GALACTICA_SPACE_1_OCLOCK,
    GALACTICA_SPACE_3_OCLOCK,
    GALACTICA_SPACE_5_OCLOCK,
    GALACTICA_SPACE_7_OCLOCK,
    GALACTICA_SPACE_9_OCLOCK,
    GALACTICA_SPACE_11_OCLOCK,

    PEGASUS_CIC,
    AIRLOCK,
    MAIN_BATTERIES,
    ENGINE_ROOM,

    CAPRICA,
    CYLON_FLEET,
    HUMAN_FLEET,
    RESURRECTION_SHIP,

    BASESTAR_BRIDGE,
    CYLON_FLEET_SPACE_1,
    CYLON_FLEET_SPACE_2,
    CYLON_FLEET_SPACE_3,
    CYLON_FLEET_SPACE_4,
    CYLON_FLEET_SPACE_5_6,
    CYLON_FLEET_SPACE_7_8;

    public boolean isSpaceLocation() {
        return SPACE_LOCATIONS.contains(this);
    }

    public boolean isHazardousLocation() {
        return HAZARDOUS_LOCATIONS.contains(this);
    }

    public boolean isCylonLocation() {
        return ordinal() >= CAPRICA.ordinal();
    }

    public List<Location> adjacentLocations() {
        assert GALACTICA_SPACE.locations().contains(this);
        return DISTANCE_LOOKUP_TABLE.get(this)
                .entrySet()
                .stream()
                .filter(es -> es.getValue() == 1)
                .map(Map.Entry::getKey)
                .toList();
    }
}
