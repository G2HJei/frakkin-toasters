package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;

import java.util.*;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.START;

@Getter
@Accessors(fluent = true)
public class GalacticaBoard extends BattlestarBoard {
    private       int                 fuel                 = 8;
    private       int                 food                 = 8;
    private       int                 morale               = 10;
    private       int                 population           = 12;
    private       JumpPreparation     jumpPreparation      = START;
    @Setter
    private       boolean             engineRoomActivated  = false;
    private       boolean             colonialOneDestroyed = false;
    private       boolean             hubDestroyed         = false;
    private       Set<Ship>           reserves             = new HashSet<>();
    private       Set<Ship>           damagedShips         = new HashSet<>();
    private final Map<Ship, Location> shipsInSpace         = new HashMap<>();


    public GalacticaBoard() {
        super(galacticaLocations());
        addToReserves(List.of(
                new Viper(1), new Viper(2), new Viper(3), new Viper(4), new Viper(5), new Viper(6), new Raptor(11),
                new Raptor(12), new Raptor(13), new Raptor(14),
                new AssaultRaptor(21)));
        addToDamagedShips(List.of(new ViperMarkVII(71), new ViperMarkVII(72), new ViperMarkVII(73), new ViperMarkVII(74)));
    }

    @Override
    public Location locate(Character character) {
        return Optional.ofNullable(super.locate(character))
                .orElseGet(() -> shipsInSpace.entrySet()
                        .stream()
                        .filter(es -> es.getKey() instanceof PilotableShip
                                && ((PilotableShip) es.getKey()).pilot() == character)
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElse(null));
    }

    private static Set<Location> galacticaLocations() {
        return new HashSet<>(Set.of(FTL_CONTROL, WEAPONS_CONTROL, COMMUNICATIONS, RESEARCH_LAB, ARMORY, COMMAND, ADMIRALS_QUARTERS, HANGAR_DECK, SICKBAY, BRIG,

                PRESS_ROOM, PRESIDENTS_OFFICE, ADMINISTRATION,

                CAPRICA, CYLON_FLEET, HUMAN_FLEET, RESURRECTION_SHIP,

                GALACTICA_SPACE_12_OCLOCK, GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK, GALACTICA_SPACE_6_OCLOCK, GALACTICA_SPACE_8_OCLOCK, GALACTICA_SPACE_10_OCLOCK));
    }

    public void destroyColonialOne() {
        locations.remove(PRESS_ROOM);
        locations.remove(PRESIDENTS_OFFICE);
        locations.remove(ADMINISTRATION);
        charactersIn(PRESS_ROOM).forEach(c -> place(SICKBAY, c));
        charactersIn(PRESIDENTS_OFFICE).forEach(c -> place(SICKBAY, c));
        charactersIn(ADMINISTRATION).forEach(c -> place(SICKBAY, c));
        colonialOneDestroyed = true;
    }

    public void destroyResurrectionShip() {
        locations.remove(RESURRECTION_SHIP);
        locations.add(HUB_DESTROYED);
        charactersIn(RESURRECTION_SHIP).forEach(c -> place(HUB_DESTROYED, c));
        hubDestroyed = true;
    }

    public GalacticaBoard addToReserves(Ship ship) {
        return addToReserves(List.of(ship));
    }

    public GalacticaBoard addToReserves(List<Ship> ships) {
        reserves.addAll(ships);
        return this;
    }

    public GalacticaBoard addToDamagedShips(Ship ship) {
        return addToDamagedShips(List.of(ship));
    }

    public GalacticaBoard addToDamagedShips(List<Ship> ships) {
        damagedShips.addAll(ships);
        return this;
    }

    public Ship removeFromReserves(ShipType shipType) {
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

    public GalacticaBoard place(Location in, Ship ship) {
        return place(in, List.of(ship));
    }

    public GalacticaBoard place(Location in, Ship... ships) {
        return place(in, Arrays.stream(ships).toList());
    }

    public GalacticaBoard place(Location in, List<Ship> ships) {
        assert locations().contains(in) && in.isSpaceLocation();
        ships.forEach(s -> shipsInSpace.put(s, in));
        ships.stream()
                .filter(s -> s instanceof PilotableShip)
                .map(PilotableShip.class::cast)
                .map(PilotableShip::pilot)
                .filter(Objects::nonNull)
                .forEach(this::remove);
        return this;
    }

    public GalacticaBoard remove(Ship ship) {
        assert shipsInSpace.containsKey(ship);
        shipsInSpace.remove(ship);
        return this;
    }

    public List<Ship> shipsIn(Location location) {
        return shipsIn(location, ShipType.values());
    }

    public List<Ship> shipsIn(Location location, ShipType... ofType) {
        val constraint = Arrays.stream(ofType).toList();
        return shipsInSpace.entrySet()
                .stream()
                .filter(e -> e.getValue() == location)
                .map(Map.Entry::getKey)
                .filter(s -> constraint.contains(s.type()))
                .toList();
    }

    public List<Ship> shipsInSpace(ShipType... ofType) {
        val constraint = Arrays.stream(ofType).toList();
        return shipsInSpace.keySet()
                .stream()
                .filter(s -> constraint.contains(s.type()))
                .toList();
    }

    public void advanceJumpPreparation() {
        val current = jumpPreparation.ordinal();
        val autoJump = JumpPreparation.values().length - 1;
        val next = current == autoJump ? 0 : current + 1;
        jumpPreparation = JumpPreparation.values()[next];
    }

    public GalacticaBoard decreaseFood() {
        food--;
        return this;
    }

    public GalacticaBoard increaseFood() {
        food++;
        return this;
    }

    public GalacticaBoard decreaseFuel() {
        fuel--;
        return this;
    }

    public GalacticaBoard increaseFuel() {
        fuel++;
        return this;
    }
}