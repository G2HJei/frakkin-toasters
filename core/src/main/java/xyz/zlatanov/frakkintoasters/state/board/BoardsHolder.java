package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class BoardsHolder {
    private final GalacticaBoard  galactica  = new GalacticaBoard();
    private final PegasusBoard    pegasus    = new PegasusBoard();
    private final CylonFleetBoard cylonFleet = new CylonFleetBoard();
}
