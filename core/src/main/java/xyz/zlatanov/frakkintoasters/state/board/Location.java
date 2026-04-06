package xyz.zlatanov.frakkintoasters.state.board;

import java.util.List;
import java.util.Map;

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

    public static final  Map<String, List<Location>> LOCATION_AREAS     = Map.of(
            "Galactica", List.of(FTL_CONTROL, WEAPONS_CONTROL, COMMUNICATIONS, RESEARCH_LAB, ARMORY, COMMAND, ADMIRALS_QUARTERS, HANGAR_DECK, SICKBAY, BRIG),
            "Galactica space", List.of(GALACTICA_SPACE_12_OCLOCK, GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK, GALACTICA_SPACE_6_OCLOCK, GALACTICA_SPACE_8_OCLOCK, GALACTICA_SPACE_10_OCLOCK),
            "Colonial One", List.of(PRESS_ROOM, PRESIDENTS_OFFICE, ADMINISTRATION),
            "Cylon locations & fleet", List.of(BASESTAR_BRIDGE, CAPRICA, CYLON_FLEET, HUMAN_FLEET, RESURRECTION_SHIP, HUB_DESTROYED),
            "Cylon fleet space", List.of(CYLON_FLEET_SPACE_1, CYLON_FLEET_SPACE_2, CYLON_FLEET_SPACE_3, CYLON_FLEET_SPACE_4, CYLON_FLEET_SPACE_5_6, CYLON_FLEET_SPACE_7_8),
            "Pegasus", List.of(PEGASUS_CIC, AIRLOCK, MAIN_BATTERIES, ENGINE_ROOM),
            "Demetrius", List.of(BRIDGE, TACTICAL_PLOT, CAPTAINS_CABIN)
    );
    private static final List<Location>              spaceLocations     = LOCATION_AREAS.get("Galactica space");
    private static final List<Location>              hazardousLocations = List.of(SICKBAY, BRIG, RESURRECTION_SHIP, HUB_DESTROYED);

    public boolean isSpaceLocation() {
        return spaceLocations.contains(this);
    }

    public boolean isHazardousLocation() {
        return hazardousLocations.contains(this);
    }

    public boolean isCylonLocation() {
        return ordinal() >= CAPRICA.ordinal();
    }
}
