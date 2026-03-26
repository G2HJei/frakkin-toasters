package xyz.zlatanov.frakkintoasters;

import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;

@Getter
@Accessors(fluent = true)
public class Game {
    private ObjectiveCard  objective;
    private GalacticaBoard galacticaBoard;

    public void objective(ObjectiveCard objective) {
        if (objective != null) {
            throw new FrakCallTheAdmiralException();
        }
        this.objective = objective;
    }

}
