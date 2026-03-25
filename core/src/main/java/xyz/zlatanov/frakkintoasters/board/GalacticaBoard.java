package xyz.zlatanov.frakkintoasters.board;

import xyz.zlatanov.frakkintoasters.Location;

import java.util.HashSet;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.Location.*;

public class GalacticaBoard extends Board {

    private boolean colonialOneDestroyed = false;

    public GalacticaBoard() {
        super(galacticaLocations());
    }


    private static Set<Location> galacticaLocations() {
        return new HashSet<>(Set.of(FTL_CONTROL, WEAPONS_CONTROL, COMMUNICATIONS, RESEARCH_LAB, ARMORY, COMMAND, ADMIRALS_QUARTERS, HANGAR_DECK, SICKBAY, BRIG,

                PRESS_ROOM, PRESIDENTS_OFFICE, ADMINISTRATION,

                CAPRICA, CYLON_FLEET, HUMAN_FLEET, RESURRECTION_SHIP,

                GALACTICA_SPACE_12_OCLOCK, GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK, GALACTICA_SPACE_6_OCLOCK, GALACTICA_SPACE_8_OCLOCK));
    }

    public void destroyColonialOne() {
        removeLocations(PRESS_ROOM, PRESIDENTS_OFFICE, ADMINISTRATION);
        moveTo(SICKBAY, charactersIn(PRESS_ROOM, PRESIDENTS_OFFICE, ADMINISTRATION));
        colonialOneDestroyed = true;
    }

    public boolean colonialOneDestroyed() {
        return colonialOneDestroyed;
    }

}
