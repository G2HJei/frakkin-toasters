package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.ship.*;
import xyz.zlatanov.frakkintoasters.state.track.BoardingParty;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;

import java.util.*;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.*;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.HUMANS_LOSE;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.START;

@Getter
@Accessors(fluent = true)
public class GalacticaBoard implements BattlestarBoard, SpaceLocationsBoard {
    private       int                           fuel                 = 8;
    private       int                           food                 = 8;
    private       int                           morale               = 10;
    private       int                           population           = 12;
    private       JumpPreparation               jumpPreparation      = JumpPreparation.START;
    @Setter
    private       boolean                       engineRoomActivated  = false;
    private       boolean                       colonialOneDestroyed = false;
    private       boolean                       hubDestroyed         = false;
    private final Map<Character, Location>      characters           = new HashMap<>();
    private final Set<Location>                 damagedLocations     = new HashSet<>();
    private final Set<Location>                 locations            = new HashSet<>(Stream.of(GALACTICA.locations(), GALACTICA_SPACE.locations(), COLONIAL_ONE.locations(), CYLON_LOCATIONS.locations().stream().filter(l -> !Set.of(HUB_DESTROYED, BASESTAR_BRIDGE).contains(l)).toList()).flatMap(Collection::stream).toList());
    private final Set<Ship>                     reserves             = new HashSet<>(Set.of(new Viper(1), new Viper(2), new Viper(3), new Viper(4), new Viper(5), new Viper(6), new Raptor(11), new Raptor(12), new Raptor(13), new Raptor(14), new AssaultRaptor(21)));
    private final Set<Ship>                     damagedShips         = new HashSet<>(Set.of(new ViperMarkVII(71), new ViperMarkVII(72), new ViperMarkVII(73), new ViperMarkVII(74)));
    private final Map<Ship, Location>           shipsInSpace         = new HashMap<>();
    private final Map<Centurion, BoardingParty> boardingPartyTrack   = new HashMap<>();

    public static final List<Location> VIPER_LAUNCH_SPACES = List.of(GALACTICA_SPACE_4_OCLOCK, GALACTICA_SPACE_6_OCLOCK);

    @Override
    public Optional<Location> locate(Character character) {
        val superLocate = Optional.ofNullable(characters().get(character));
        return superLocate.isPresent()
                ? superLocate
                : shipsInSpace.entrySet()
                .stream()
                .filter(es -> es.getKey() instanceof HumanFighter
                              && ((HumanFighter) es.getKey()).pilot() == character)
                .findFirst()
                .map(Map.Entry::getValue);
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


    public <T extends Ship> Optional<T> removeFromReserves(Class<T> shipClass) {
        return removeFrom(reserves, shipClass);
    }

    public <T extends Ship> Optional<T> removeFromDamagedShips(Class<T> shipClass) {
        return removeFrom(damagedShips, shipClass);
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

    public GalacticaBoard advanceJumpPreparation() {
        val current = jumpPreparation.ordinal();
        val autoJump = JumpPreparation.values().length - 1;
        val next = current == autoJump ? 0 : current + 1;
        jumpPreparation = JumpPreparation.values()[next];
        return this;
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

    private <T extends Ship> Optional<T> removeFrom(Set<Ship> source, Class<T> shipClass) {
        return source.stream()
                .filter(s -> shipClass.equals(s.getClass()))
                .findFirst()
                .map(ship -> {
                    source.remove(ship);
                    return shipClass.cast(ship);
                });
    }
}