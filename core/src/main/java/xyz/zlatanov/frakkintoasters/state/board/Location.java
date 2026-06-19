package xyz.zlatanov.frakkintoasters.state.board;

import java.util.List;
import java.util.Map;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.*;

public enum Location {
    //galactica
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

    //colonial one
    PRESS_ROOM,
    PRESIDENTS_OFFICE,
    ADMINISTRATION,

    //galactica space
    GALACTICA_SPACE_12_OCLOCK,
    GALACTICA_SPACE_2_OCLOCK,
    GALACTICA_SPACE_4_OCLOCK,
    GALACTICA_SPACE_6_OCLOCK,
    GALACTICA_SPACE_8_OCLOCK,
    GALACTICA_SPACE_10_OCLOCK,

    //pegasus
    PEGASUS_CIC,
    AIRLOCK,
    MAIN_BATTERIES,
    ENGINE_ROOM,

    //demetrius
    BRIDGE,
    TACTICAL_PLOT,
    CAPTAINS_CABIN,

    //cylon
    CAPRICA,
    CYLON_FLEET,
    HUMAN_FLEET,
    RESURRECTION_SHIP,
    HUB_DESTROYED,

    //cylon fleet
    BASESTAR_BRIDGE,
    CYLON_FLEET_SPACE_1,
    CYLON_FLEET_SPACE_2,
    CYLON_FLEET_SPACE_3,
    CYLON_FLEET_SPACE_4,
    CYLON_FLEET_SPACE_5_6,
    CYLON_FLEET_SPACE_7_8;

    //new caprica
    //MEDICAL_CENTER,
    //RESISTANCE_HQ,
    //DETENTION,
    //OCCUPATION_AUTHORITY,
    //BREEDERS_CANYON,
    //SHIPYARD;

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
