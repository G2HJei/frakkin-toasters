package xyz.zlatanov.frakkintoasters;

import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.board.PegasusBoard;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;

@Getter
@Accessors(fluent = true)
public class Game {
    private ObjectiveCard objective;
    private GalacticaBoard galacticaBoard = new GalacticaBoard();
    private PegasusBoard pegasusBoard = new PegasusBoard();
    private CylonFleetBoard cylonFleetBoard = new CylonFleetBoard();

    public void objective(ObjectiveCard objective) {
        if (this.objective != null) {
            throw new FrakCallTheAdmiralException();
        }
        this.objective = objective;
    }

}
