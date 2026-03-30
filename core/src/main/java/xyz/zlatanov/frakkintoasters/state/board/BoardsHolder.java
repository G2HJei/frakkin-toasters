package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public class BoardsHolder {
    private final GalacticaBoard  galactica  = new GalacticaBoard();
    private final PegasusBoard    pegasus    = new PegasusBoard();
    private final CylonFleetBoard cylonFleet = new CylonFleetBoard();

    public List<Board> all() {
        return List.of(galactica, pegasus, cylonFleet);
    }
}
