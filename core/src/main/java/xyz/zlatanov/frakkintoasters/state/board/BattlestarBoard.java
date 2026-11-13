package xyz.zlatanov.frakkintoasters.state.board;

import xyz.zlatanov.frakkintoasters.state.Mutates;
import xyz.zlatanov.frakkintoasters.state.view.BattlestarBoardView;

import java.util.Set;

public interface BattlestarBoard extends Board, BattlestarBoardView {

    Set<Location> damagedLocations();

    @Mutates
    BattlestarBoard damage(Location location);

    @Override
    default boolean isDamaged(Location location) {
        return damagedLocations().contains(location);
    }

    //todo add repair to be used by the corresponding engineering(?) card
}
