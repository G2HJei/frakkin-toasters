package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Mutates;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;
import xyz.zlatanov.frakkintoasters.state.track.BoardingParty;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;
import xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toCollection;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BASESTAR_BRIDGE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.*;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.HUMANS_LOSE;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.START;

@Getter //todo replace getters with explicit methods for View interface usage to ensure no modification is possible
@Accessors(fluent = true)
public class GalacticaBoard implements BattlestarBoard, SpaceLocationsBoard, GalacticaBoardView {
    private static final int RESOURCE_MAXIMUM = 15; // rule 13.2.1

    private       int                               fuel                 = 8;
    private       int                               food                 = 8;
    private       int                               morale               = 10;
    private       int                               population           = 12;
    private       JumpPreparation                   jumpPreparation      = JumpPreparation.START;
    private       boolean                           colonialOneDestroyed = false;
    private final Map<Character, Location>          characters           = new HashMap<>();
    private final Set<Location>                     damagedLocations     = new HashSet<>();
    private final Set<Location>                     locations            = new HashSet<>(Stream.of(GALACTICA.locations(), GALACTICA_SPACE.locations(), COLONIAL_ONE.locations(), CYLON_LOCATIONS.locations().stream().filter(l -> l != BASESTAR_BRIDGE).toList()).flatMap(Collection::stream).toList());
    private final TreeSet<Ship>                     reserves             = Stream.of(new Viper(1), new Viper(2), new Viper(3), new Viper(4), new Viper(5), new Viper(6), new Raptor(11), new Raptor(12), new Raptor(13), new Raptor(14), new AssaultRaptor(21)).collect(toCollection(() -> new TreeSet<>(comparingInt(Ship::id))));
    private final Set<Viper>                        damagedShips         = new HashSet<>(Set.of(new ViperMarkVII(71), new ViperMarkVII(72), new ViperMarkVII(73), new ViperMarkVII(74)));
    private final Map<Ship, Location>               shipsInSpace         = new HashMap<>();
    private final TreeMap<Centurion, BoardingParty> boardingPartyTrack   = new TreeMap<>(comparingInt(Centurion::id));

    @Override
    public Set<Ship> reserves() {
        return new TreeSet<>(reserves);
    }

    @Override
    public Map<Centurion, BoardingParty> boardingPartyTrack() {
        return new TreeMap<>(boardingPartyTrack);
    }

    @Override
    public Optional<Location> locate(Character character) {
        return Optional.ofNullable(characters().get(character))
                .or(() -> pilotedFighter(character).map(shipsInSpace::get));
    }

    @Override
    public Optional<HumanFighter> pilotedFighter(Character character) {
        return shipsInSpace.keySet()
                .stream()
                .filter(s -> s instanceof HumanFighter fighter && fighter.pilot() == character)
                .map(HumanFighter.class::cast)
                .findFirst();
    }

    @Mutates
    public void destroyColonialOne() {
        for (val location : COLONIAL_ONE.locations()) {
            charactersIn(location).forEach(c -> place(SICKBAY, c));
            locations.remove(location);
        }
        colonialOneDestroyed = true;
    }

    @Mutates
    public GalacticaBoard addToReserves(Ship ship) {
        return addToReserves(List.of(ship));
    }

    @Mutates
    public GalacticaBoard addToReserves(List<Ship> ships) {
        reserves.addAll(ships);
        return this;
    }

    @Mutates
    public GalacticaBoard addToDamagedShips(Viper ship) {
        return addToDamagedShips(List.of(ship));
    }

    @Mutates
    public GalacticaBoard addToDamagedShips(List<Viper> ships) {
        damagedShips.addAll(ships);
        return this;
    }


    @Mutates
    public <T extends Ship> Optional<T> removeFromReserves(Class<T> shipClass) {
        return removeFrom(reserves, shipClass);
    }

    @Mutates
    public GalacticaBoard removeFromReserves(Ship ship) {
        reserves.remove(ship);
        return this;
    }

    @Mutates
    public Optional<HumanFighter> removeFromReserves(int shipId) {
        val reservesFighter = reserves.stream()
                .filter(s -> s instanceof HumanFighter)
                .map(HumanFighter.class::cast)
                .filter(s -> s.id() == shipId)
                .findFirst();
        reservesFighter.ifPresent(reserves::remove);
        return reservesFighter;
    }

    @Mutates
    public GalacticaBoard removeFromDamagedShips(Ship ship) {
        damagedShips.remove(ship);
        return this;
    }

    @Mutates
    public <T extends Viper> Optional<T> removeFromDamagedShips(Class<T> shipClass) {
        return removeFrom(damagedShips, shipClass);
    }

    @Mutates
    public GalacticaBoard boardGalactica(Centurion centurion) {
        boardingPartyTrack.put(centurion, START);
        return this;
    }

    @Mutates
    public GalacticaBoard advanceBoardingParty() {
        assert !boardingPartyTrack.containsValue(HUMANS_LOSE);
        boardingPartyTrack.replaceAll(
                (c, pos) ->
                        BoardingParty.values()[pos.ordinal() + 1]);
        return this;
    }

    @Mutates
    public GalacticaBoard advanceJumpPreparation() {
        val current = jumpPreparation.ordinal();
        val autoJump = JumpPreparation.values().length - 1;
        val next = current == autoJump ? 0 : current + 1;
        jumpPreparation = JumpPreparation.values()[next];
        return this;
    }

    @Mutates
    public GalacticaBoard decreaseJumpPreparation() {
        jumpPreparation = JumpPreparation.values()[Math.max(0, jumpPreparation.ordinal() - 1)];
        return this;
    }

    @Mutates
    public Centurion destroyCenturion(int centurionId) {
        val centurion = boardingPartyTrack.keySet().stream()
                .filter(c -> centurionId == c.id())
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
        boardingPartyTrack.remove(centurion);
        return centurion;
    }

    @Mutates
    public GalacticaBoard decreaseFood(int amount) {
        food = Math.max(0, food - amount);
        return this;
    }

    @Mutates
    public GalacticaBoard increaseFood(int amount) {
        food = Math.min(RESOURCE_MAXIMUM, food + amount);
        return this;
    }

    @Mutates
    public GalacticaBoard decreaseFuel(int amount) {
        fuel = Math.max(0, fuel - amount);
        return this;
    }

    @Mutates
    public GalacticaBoard increaseFuel(int amount) {
        fuel = Math.min(RESOURCE_MAXIMUM, fuel + amount);
        return this;
    }

    @Mutates
    public GalacticaBoard decreaseMorale(int amount) {
        morale = Math.max(0, morale - amount);
        return this;
    }

    @Mutates
    public GalacticaBoard increaseMorale(int amount) {
        morale = Math.min(RESOURCE_MAXIMUM, morale + amount);
        return this;
    }

    @Mutates
    public GalacticaBoard decreasePopulation(int amount) {
        population = Math.max(0, population - amount);
        return this;
    }

    @Mutates
    public GalacticaBoard increasePopulation(int amount) {
        population = Math.min(RESOURCE_MAXIMUM, population + amount);
        return this;
    }

    private <T extends Ship> Optional<T> removeFrom(Set<? extends Ship> source, Class<T> shipClass) {
        return source.stream()
                .filter(s -> shipClass.equals(s.getClass()))
                .findFirst()
                .map(ship -> {
                    source.remove(ship);
                    return shipClass.cast(ship);
                });
    }
}