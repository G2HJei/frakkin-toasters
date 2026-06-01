package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

@Getter
@Accessors(fluent = true)
public enum LocationsArea {
    GALACTICA(
            FTL_CONTROL, WEAPONS_CONTROL, COMMUNICATIONS, RESEARCH_LAB, ARMORY, COMMAND, ADMIRALS_QUARTERS, HANGAR_DECK, SICKBAY, BRIG),
    GALACTICA_SPACE( // in clockwise order
            GALACTICA_SPACE_12_OCLOCK, GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK, GALACTICA_SPACE_6_OCLOCK, GALACTICA_SPACE_8_OCLOCK, GALACTICA_SPACE_10_OCLOCK),
    COLONIAL_ONE(
            PRESS_ROOM, PRESIDENTS_OFFICE, ADMINISTRATION),
    CYLON_LOCATIONS(
            BASESTAR_BRIDGE, CAPRICA, CYLON_FLEET, HUMAN_FLEET, RESURRECTION_SHIP, HUB_DESTROYED),
    CYLON_FLEET_SPACE(
            CYLON_FLEET_SPACE_1, CYLON_FLEET_SPACE_2, CYLON_FLEET_SPACE_3, CYLON_FLEET_SPACE_4, CYLON_FLEET_SPACE_5_6, CYLON_FLEET_SPACE_7_8),
    PEGASUS(
            PEGASUS_CIC, AIRLOCK, MAIN_BATTERIES, ENGINE_ROOM),
    DEMETRIUS(
            BRIDGE, TACTICAL_PLOT, CAPTAINS_CABIN);

    private final List<Location> locations;

    LocationsArea(Location... locations) {
        this.locations = List.of(locations);
    }

}
