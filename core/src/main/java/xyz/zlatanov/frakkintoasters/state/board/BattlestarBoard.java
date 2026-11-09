package xyz.zlatanov.frakkintoasters.state.board;

import xyz.zlatanov.frakkintoasters.state.Mutates;
import xyz.zlatanov.frakkintoasters.state.view.BattlestarBoardView;

import java.util.Set;

public interface BattlestarBoard extends Board, BattlestarBoardView {

    Set<Location> damagedLocations();

    @Override
    default boolean isDamaged(Location location) {
        return damagedLocations().contains(location);
    }

    @Mutates
    default BattlestarBoard damage(Location location) {
        assert locations().contains(location);
        damagedLocations().add(location);
        return this;
    }

    //todo add repair to be used by the corresponding engineering(?) card
}
