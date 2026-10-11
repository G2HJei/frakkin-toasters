package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.state.Game;

public class GameOps extends AbstractOps {

    public GameOps(Game game) {
        super(game);
    }

    public PlayerOps player(int playerNumber) {
        return new PlayerOps(game, playerNumber);
    }

    public ShipOps ship(int shipId) {
        return new ShipOps(game, shipId);
    }
}
