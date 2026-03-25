package xyz.zlatanov.frakkintoasters;

import java.util.HashSet;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.Location.*;

public class GalacticaBoard {
    private final Set<Location> locations;
    private boolean colonialOneDestroyed = false;

    public GalacticaBoard() {
        locations = galacticaLocations();
    }

    public Set<Location> locations() {
        return new HashSet<>(locations);
    }

    private Set<Location> galacticaLocations() {
        return new HashSet<>(Set.of(
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

                PRESS_ROOM,
                PRESIDENTS_OFFICE,
                ADMINISTRATION,

                CAPRICA,
                CYLON_FLEET,
                HUMAN_FLEET,
                RESURRECTION_SHIP,

                GALACTICA_SPACE_12_OCLOCK,
                GALACTICA_SPACE_2_OCLOCK,
                GALACTICA_SPACE_4_OCLOCK,
                GALACTICA_SPACE_6_OCLOCK,
                GALACTICA_SPACE_8_OCLOCK));
    }

    public void destroyColonialOne() {
        locations.removeAll(Set.of(PRESS_ROOM, PRESIDENTS_OFFICE, ADMINISTRATION));
        colonialOneDestroyed = true;
    }

    public boolean colonialOneDestroyed() {
        return colonialOneDestroyed;
    }

}
