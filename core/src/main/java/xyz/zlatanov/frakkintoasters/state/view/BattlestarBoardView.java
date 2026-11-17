package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.Set;

public interface BattlestarBoardView extends BoardView {

    Set<Location> damagedLocations();

    boolean isDamaged(Location location);
}
