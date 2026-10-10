package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.view.BoardsHolderView;

import java.util.List;

@Getter
@Accessors(fluent = true)
public class BoardsHolder implements BoardsHolderView {
    private final GalacticaBoard  galactica  = new GalacticaBoard();
    private final PegasusBoard    pegasus    = new PegasusBoard();
    private final CylonFleetBoard cylonFleet = new CylonFleetBoard();

    public List<Board> all() {
        return List.of(galactica, pegasus, cylonFleet);
    }
}
