package xyz.zlatanov.frakkintoasters.board;

import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.Location;
import xyz.zlatanov.frakkintoasters.ship.*;

import java.util.HashSet;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.Location.*;

@Getter
@Accessors(fluent = true)
public class GalacticaBoard extends Board {
    private int food = 8;
    private int morale = 10;
    private int population = 12;
    private boolean colonialOneDestroyed = false;
    private Set<Ship> reserves = new HashSet<>();
    private Set<FighterShip> damagedShips = new HashSet<>();

    public GalacticaBoard() {
        super(galacticaLocations());
        reserves.addAll(Set.of(
                new Viper(), new Viper(), new Viper(), new Viper(),
                new Raptor(), new Raptor(), new Raptor(), new Raptor(),
                new AssaultRaptor()
        ));
        damagedShips.addAll(Set.of(
                new ViperMarkVII(), new ViperMarkVII(), new ViperMarkVII(), new ViperMarkVII()
        ));
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

}
