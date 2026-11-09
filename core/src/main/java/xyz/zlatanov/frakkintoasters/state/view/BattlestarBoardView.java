package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.board.Location;

public interface BattlestarBoardView extends BoardView {

    boolean isDamaged(Location location);
}
