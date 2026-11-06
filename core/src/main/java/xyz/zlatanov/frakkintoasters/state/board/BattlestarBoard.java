package xyz.zlatanov.frakkintoasters.state.board;

import xyz.zlatanov.frakkintoasters.state.Mutates;

import java.util.Set;

public interface BattlestarBoard extends Board {

    Set<Location> damagedLocations();

    @Mutates
    default BattlestarBoard damage(Location location) {
        assert locations().contains(location);
        damagedLocations().add(location);
        return this;
    }

    //todo add repair to be used by the corresponding engineering(?) card
}
