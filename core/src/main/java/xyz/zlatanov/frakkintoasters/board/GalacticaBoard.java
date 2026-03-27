package xyz.zlatanov.frakkintoasters.board;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.Location;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.ship.*;

import java.util.*;

import static java.util.Collections.newSetFromMap;
import static xyz.zlatanov.frakkintoasters.Location.*;

@Getter
@Accessors(fluent = true)
public class GalacticaBoard extends Board {
    private       int                 food                 = 8;
    private       int                 morale               = 10;
    private       int                 population           = 12;
    private       boolean             colonialOneDestroyed = false;
    private       Set<Ship>           reserves             = newSetFromMap(new IdentityHashMap<>());
    private       Set<Ship>           damagedShips         = newSetFromMap(new IdentityHashMap<>());
    private final Map<Ship, Location> launchedFighters     = new IdentityHashMap<>();


    public GalacticaBoard() {
        super(galacticaLocations());
        reserves.addAll(List.of(
                new Viper(), new Viper(), new Viper(), new Viper(),
                new Raptor(), new Raptor(), new Raptor(), new Raptor(),
                new AssaultRaptor()
        ));
        damagedShips.addAll(List.of(
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
        //todo move this to crisis card
        removeLocations(PRESS_ROOM, PRESIDENTS_OFFICE, ADMINISTRATION);
        move(SICKBAY, charactersIn(PRESS_ROOM, PRESIDENTS_OFFICE, ADMINISTRATION));
        colonialOneDestroyed = true;
    }

    public Ship removeFromReserve(ShipType shipType) {
        return popFrom(reserves, shipType);
    }

    public Ship removeFromDamagedShips(ShipType shipType) {
        return popFrom(damagedShips, shipType);
    }

    private static Ship popFrom(Set<Ship> source, ShipType shipType) {
        val ship = source.stream()
                .filter(s -> s.type() == shipType)
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
        source.remove(ship);
        return ship;
    }
}