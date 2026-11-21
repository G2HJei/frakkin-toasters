package xyz.zlatanov.frakkintoasters.state.board;

import xyz.zlatanov.frakkintoasters.state.Mutates;
import xyz.zlatanov.frakkintoasters.state.view.BattlestarBoardView;

import java.util.Set;

public interface BattlestarBoard extends Board, BattlestarBoardView {

    @Mutates
    BattlestarBoard damage(Location location);

    @Override
    default boolean isDamaged(Location location) {
        return damagedLocations().contains(location);
    }

    @Mutates
    BattlestarBoard repair(Location location);
}
