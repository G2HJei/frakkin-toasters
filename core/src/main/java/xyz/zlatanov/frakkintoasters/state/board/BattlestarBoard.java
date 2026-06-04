package xyz.zlatanov.frakkintoasters.state.board;

import java.util.Set;

public interface BattlestarBoard extends Board {

    Set<Location> damagedLocations();

    default BattlestarBoard damage(Location location) {
        assert locations().contains(location);
        damagedLocations().add(location);
        return this;
    }

}
