package xyz.zlatanov.frakkintoasters.board;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.Location;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.exception.InvalidMoveLocationException;
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
    private final Map<Ship, Location> shipsInSpace         = new IdentityHashMap<>();


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
        //todo setup civilian ships in Game, move this setup there too?
        place(GALACTICA_SPACE_4_OCLOCK, new Viper());
        place(GALACTICA_SPACE_6_OCLOCK, new Viper());
        place(GALACTICA_SPACE_8_OCLOCK, new Basestar());
        place(GALACTICA_SPACE_8_OCLOCK, List.of(new Raider(), new Raider(), new Raider()));
    }


    private static Set<Location> galacticaLocations() {
        return new HashSet<>(Set.of(FTL_CONTROL, WEAPONS_CONTROL, COMMUNICATIONS, RESEARCH_LAB, ARMORY, COMMAND, ADMIRALS_QUARTERS, HANGAR_DECK, SICKBAY, BRIG,

                PRESS_ROOM, PRESIDENTS_OFFICE, ADMINISTRATION,

                CAPRICA, CYLON_FLEET, HUMAN_FLEET, RESURRECTION_SHIP,

                GALACTICA_SPACE_12_OCLOCK, GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK, GALACTICA_SPACE_6_OCLOCK, GALACTICA_SPACE_8_OCLOCK));
    }

    public void destroyColonialOne() {
        //todo move this to crisis card
        remove(PRESS_ROOM);
        remove(PRESIDENTS_OFFICE);
        remove(ADMINISTRATION);
        charactersIn(PRESS_ROOM).forEach(c -> place(SICKBAY, c));
        charactersIn(PRESIDENTS_OFFICE).forEach(c -> place(SICKBAY, c));
        charactersIn(ADMINISTRATION).forEach(c -> place(SICKBAY, c));
        colonialOneDestroyed = true;
    }

    public Ship removeFromReserve(ShipType shipType) {
        return removeFrom(reserves, shipType);
    }

    public Ship removeFromDamagedShips(ShipType shipType) {
        return removeFrom(damagedShips, shipType);
    }

    private static <T extends Ship> T removeFrom(Set<T> source, ShipType shipType) {
        val ship = source.stream()
                .filter(s -> s.type() == shipType)
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
        source.remove(ship);
        return ship;
    }

    public void place(Location in, Ship ship) {
        place(in, List.of(ship));
    }

    public void place(Location in, List<Ship> ships) {
        if (!locations().contains(in) || !in.isSpaceLocation()) {
            throw new InvalidMoveLocationException();
        }
        ships.forEach(s -> shipsInSpace.put(s, in));
    }

    public List<Ship> shipsIn(Location location) {
        return shipsInSpace.entrySet()
                .stream()
                .filter(e -> e.getValue() == location)
                .map(Map.Entry::getKey)
                .toList();
    }
}