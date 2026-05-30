package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;
import xyz.zlatanov.frakkintoasters.state.track.BoardingParty;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;

import java.util.*;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.HUMANS_LOSE;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.START;

@Getter
@Accessors(fluent = true)
public class GalacticaBoard extends BattlestarBoard {
    private       int                           fuel                 = 8;
    private       int                           food                 = 8;
    private       int                           morale               = 10;
    private       int                           population           = 12;
    private       JumpPreparation               jumpPreparation      = JumpPreparation.START;
    @Setter
    private       boolean                       engineRoomActivated  = false;
    private       boolean                       colonialOneDestroyed = false;
    private       boolean                       hubDestroyed         = false;
    private final Set<Ship>                     reserves             = new HashSet<>();
    private final Set<Ship>                     damagedShips         = new HashSet<>();
    private final Map<Ship, Location>           shipsInSpace         = new HashMap<>();
    private final Map<Centurion, BoardingParty> boardingPartyTrack   = new HashMap<>();

    public static final List<Location> VIPER_LAUNCH_SPACES = List.of(GALACTICA_SPACE_4_OCLOCK, GALACTICA_SPACE_6_OCLOCK);


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
                        .filter(es -> es.getKey() instanceof HumanFighter
                                && ((HumanFighter) es.getKey()).pilot() == character)
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

    //todo usee Class instead of enum for type safety

    public <T extends Ship> T removeFromReserves(Class<T> shipClass) {
        return removeFrom(reserves, shipClass);
    }

    public <T extends Ship> T removeFromDamagedShips(Class<T> shipClass) {
        return removeFrom(damagedShips, shipClass);
    }

    public GalacticaBoard place(Location in, Ship... ships) {
        return place(in, Arrays.stream(ships).toList());
    }

    public GalacticaBoard place(Location in, List<Ship> ships) {
        assert locations().contains(in) && in.isSpaceLocation();
        ships.forEach(s -> shipsInSpace.put(s, in));
        ships.stream()
                .filter(s -> s instanceof HumanFighter)
                .map(HumanFighter.class::cast)
                .map(HumanFighter::pilot)
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
        return shipsInSpace.entrySet()
                .stream()
                .filter(e -> e.getValue() == location)
                .map(Map.Entry::getKey)
                .toList();
    }

    public <T extends Ship> List<T> shipsIn(Location location, Class<T> shipClass) {
        return shipsInSpace.entrySet()
                .stream()
                .filter(e -> e.getValue() == location)
                .map(Map.Entry::getKey)
                .filter(s -> shipClass.equals(s.getClass()))
                .map(shipClass::cast)
                .toList();
    }

    public <T extends Ship> List<Ship> shipsIn(Location location, List<Class<T>> shipClasses) {
        return shipsInSpace.entrySet()
                .stream()
                .filter(e -> e.getValue() == location)
                .map(Map.Entry::getKey)
                .filter(s -> shipClasses.contains(s.getClass()))
                .toList();
    }

    public <T extends Ship> T shipInSpace(int shipId, Class<T> shipClass) {
        return shipsInSpace(shipClass).stream()
                .filter(r -> r.id() == shipId)
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
    }

    public <T extends Ship> List<T> shipsInSpace(Class<T> shipClass) {
        return shipsInSpace.keySet()
                .stream()
                .filter(s -> shipClass.equals(s.getClass()))
                .map(shipClass::cast)
                .toList();
    }

    public List<HumanFighter> humanFightersIn(Location location) {
        return shipsIn(location).stream()
                .filter(HumanFighter.class::isInstance)
                .map(HumanFighter.class::cast)
                .toList();
    }

    public Location locate(Ship ship) {
        return shipsInSpace.get(ship);
    }

    public GalacticaBoard boardGalactica(Centurion centurion) {
        boardingPartyTrack.put(centurion, START);
        return this;
    }

    public GalacticaBoard advanceBoardingParty() {
        assert !boardingPartyTrack.containsValue(HUMANS_LOSE);
        boardingPartyTrack.replaceAll(
                (c, pos) ->
                        BoardingParty.values()[pos.ordinal() + 1]);
        return this;
    }

    public void advanceJumpPreparation() {
        val current = jumpPreparation.ordinal();
        val autoJump = JumpPreparation.values().length - 1;
        val next = current == autoJump ? 0 : current + 1;
        jumpPreparation = JumpPreparation.values()[next];
    }

    public GalacticaBoard decreaseFood(int amount) {
        food = Math.max(0, food - amount);
        return this;
    }

    public GalacticaBoard increaseFood() {
        food++;
        return this;
    }

    public GalacticaBoard decreaseFuel(int amount) {
        fuel = Math.max(0, fuel - amount);
        return this;
    }

    public GalacticaBoard increaseFuel() {
        fuel++;
        return this;
    }

    public GalacticaBoard decreaseMorale(int amount) {
        morale = Math.max(0, morale - amount);
        return this;
    }

    public GalacticaBoard decreasePopulation(int amount) {
        population = Math.max(0, population - amount);
        return this;
    }

    private <T extends Ship> T removeFrom(Set<Ship> source, Class<T> shipClass) {
        val ship = source.stream()
                .filter(s -> shipClass.equals(s.getClass()))
                .findFirst()
                .map(shipClass::cast)
                .orElse(null);
        source.remove(ship);
        return ship;
    }
}