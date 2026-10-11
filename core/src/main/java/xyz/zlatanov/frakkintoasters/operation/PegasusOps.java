package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.BattlestarBoard;
import xyz.zlatanov.frakkintoasters.state.board.Location;

public class PegasusOps extends BattlestarOps {

    PegasusOps(Game game) {
        super(game);
    }

    public void damage() {
        val pegasusDamage = game.decks().pegasusDamage().draw();
        damageLocation(Location.valueOf(pegasusDamage.name()));
    }

    @Override
    protected BattlestarBoard board() {
        return game.boards().pegasus();
    }
}
