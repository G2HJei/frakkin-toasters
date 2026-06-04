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

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.HUMANS_LOSE;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.START;

@Getter
@Accessors(fluent = true)
public class GalacticaBoard extends BattlestarBoard implements SpaceLocationsBoard {
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
    public Optional<Location> locate(Character character) {
        val superLocate = super.locate(character);
        return superLocate.isPresent()
                ? superLocate
                : shipsInSpace.entrySet()//todo make parent classes interfaces now!!!
                .stream()
                .filter(es -> es.getKey() instanceof HumanFighter
                              && ((HumanFighter) es.getKey()).pilot() == character)
                .findFirst()
                .map(Map.Entry::getValue);
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