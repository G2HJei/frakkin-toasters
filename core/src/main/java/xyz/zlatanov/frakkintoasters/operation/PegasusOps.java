package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.PegasusBoard;

public class PegasusOps extends BattlestarOps {

    PegasusOps(Game game) {
        super(game);
    }

    public void damage() {
        val pegasusDamage = game.decks().pegasusDamage().draw();
        damageLocation(Location.valueOf(pegasusDamage.name()));
    }

    public void activateEngineRoom() {
        board().engineRoomActivated(true);
    }

    @Override
    protected PegasusBoard board() {
        return game.boards().pegasus();
    }
}
