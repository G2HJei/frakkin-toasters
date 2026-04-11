package xyz.zlatanov.frakkintoasters.state.board;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class BattlestarBoard extends Board {

    private final Set<Location> damagedLocations = new HashSet<>();

    public BattlestarBoard(Set<Location> locations) {
        super(locations);
    }

    public BattlestarBoard damage(Location location) {
        assert locations.contains(location);
        damagedLocations.add(location);
        return this;
    }

    public Set<Location> damagedLocations() {
        return Collections.unmodifiableSet(damagedLocations);
    }
}
