package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.BattlestarBoard;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;

abstract class BattlestarOps extends GameOps {

    BattlestarOps(Game game) {
        super(game);
    }

    protected abstract BattlestarBoard board();

    protected void damageLocation(Location location) {
        board().damage(location)
                .charactersIn(location)
                .forEach(character -> new CharacterOps(game, character).moveTo(SICKBAY));
    }
}
