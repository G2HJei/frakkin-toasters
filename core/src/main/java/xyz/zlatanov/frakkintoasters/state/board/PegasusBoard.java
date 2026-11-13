package xyz.zlatanov.frakkintoasters.state.board;

import xyz.zlatanov.frakkintoasters.state.Mutates;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.view.PegasusBoardView;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.PEGASUS;

public class PegasusBoard implements BattlestarBoard, PegasusBoardView {

    // Declared with their concrete types, so the getters' copies keep the order, and EnumSet.copyOf(EnumSet) works on an
    // empty set too.
    private final EnumSet<Location>            locations           = EnumSet.copyOf(PEGASUS.locations());
    private final EnumSet<Location>            damagedLocations    = EnumSet.noneOf(Location.class);
    private final EnumMap<Character, Location> characters          = new EnumMap<>(Character.class);
    private       boolean                      engineRoomActivated = false;

    @Override
    public Set<Location> locations() {
        return EnumSet.copyOf(locations);
    }

    @Override
    public Set<Location> damagedLocations() {
        return EnumSet.copyOf(damagedLocations);
    }

    @Override
    public Map<Character, Location> characters() {
        return new EnumMap<>(characters);
    }

    @Override
    public boolean engineRoomActivated() {
        return engineRoomActivated;
    }

    @Mutates
    public PegasusBoard engineRoomActivated(boolean engineRoomActivated) {
        this.engineRoomActivated = engineRoomActivated;
        return this;
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
    public PegasusBoard damage(Location location) {
        assert locations.contains(location);
        damagedLocations.add(location);
        return this;
    }
}
