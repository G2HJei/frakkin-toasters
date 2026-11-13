package xyz.zlatanov.frakkintoasters.state.board;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Mutates;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.track.Pursuit;
import xyz.zlatanov.frakkintoasters.state.view.CylonFleetBoardView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.CYLON_FLEET_SPACE;
import static xyz.zlatanov.frakkintoasters.state.track.Pursuit.AUTO_ATTACK;
import static xyz.zlatanov.frakkintoasters.state.track.Pursuit.START;

public class CylonFleetBoard implements Board, SpaceLocationsBoard, CylonFleetBoardView {
    public static final Map<Location, Location> MOVE_TO_GALACTICA_MAP = Map.of(
            CYLON_FLEET_SPACE_1, GALACTICA_SPACE_2_OCLOCK,
            CYLON_FLEET_SPACE_2, GALACTICA_SPACE_4_OCLOCK,
            CYLON_FLEET_SPACE_3, GALACTICA_SPACE_6_OCLOCK,
            CYLON_FLEET_SPACE_4, GALACTICA_SPACE_8_OCLOCK,
            CYLON_FLEET_SPACE_5_6, GALACTICA_SPACE_10_OCLOCK,
            CYLON_FLEET_SPACE_7_8, GALACTICA_SPACE_12_OCLOCK);

    private       Pursuit                      pursuitTrack     = START;
    // Declared with their concrete types, so the getters' copies keep the order: new TreeMap<>(TreeMap) keeps the
    // comparator, and EnumSet.copyOf(EnumSet) works on an empty set too.
    private final EnumMap<Character, Location> characters       = new EnumMap<>(Character.class);
    private final EnumSet<Location>            locations        = Stream.of(CYLON_FLEET_SPACE.locations(), List.of(BASESTAR_BRIDGE)).flatMap(Collection::stream).collect(toCollection(() -> EnumSet.noneOf(Location.class)));
    private final EnumSet<Location>            damagedLocations = EnumSet.noneOf(Location.class);
    private final TreeMap<Ship, Location>      shipsInSpace     = new TreeMap<>(Ship.BY_ID);

    @Override
    public Pursuit pursuitTrack() {
        return pursuitTrack;
    }

    @Override
    public Map<Character, Location> characters() {
        return new EnumMap<>(characters);
    }

    @Override
    public Set<Location> locations() {
        return EnumSet.copyOf(locations);
    }

    public Set<Location> damagedLocations() {
        return EnumSet.copyOf(damagedLocations);
    }

    @Override
    public Map<Ship, Location> shipsInSpace() {
        return new TreeMap<>(shipsInSpace);
    }

    @Override
    public List<HumanFighterView> humanFightersIn(Location location) {
        throw new UnsupportedOperationException();
    }

    @Mutates
    public Pursuit advancePursuit() {
        assert pursuitTrack != AUTO_ATTACK;
        val current = pursuitTrack.ordinal();
        pursuitTrack = Pursuit.values()[current + 1];
        return pursuitTrack;
    }

    @Mutates
    public void resetPursuit() {
        pursuitTrack = START;
    }

    @Mutates
    @Override
    public void place(Location to, Character character) {
        assert locations.contains(to) && !to.isSpaceLocation();
        characters.put(character, to);
    }

    @Mutates
    @Override
    public void remove(Character character) {
        characters.remove(character);
    }

    @Mutates
    @Override
    public CylonFleetBoard place(Location in, Ship ship) {
        assert in.isSpaceLocation();
        shipsInSpace.put(ship, in);
        return this;
    }

    @Mutates
    @Override
    public CylonFleetBoard remove(Ship ship) {
        assert shipsInSpace.containsKey(ship);
        shipsInSpace.remove(ship);
        return this;
    }
}
