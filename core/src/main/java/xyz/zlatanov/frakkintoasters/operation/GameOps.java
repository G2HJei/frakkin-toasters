package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

public class GameOps extends AbstractOps {

    public GameOps(Game game) {
        super(game);
    }

    public PlayerOps player(Character character) {
        for (val p : game.players()) {
            if (p.character() == character) {
                return player(p.number());
            }
        }
        throw new FrakCallTheAdmiralException();
    }

    public PlayerOps player(int playerNumber) {
        return new PlayerOps(game, playerNumber);
    }

    public ShipOps ship(int shipId) {
        return new ShipOps(game, shipId);
    }

    public GalacticaOps galactica() {
        return new GalacticaOps(game);
    }

    public PegasusOps pegasus() {
        return new PegasusOps(game);
    }

    public CylonFleetOps cylonFleet() {
        return new CylonFleetOps(game);
    }

    public SkillCheckOps skillCheck() {
        return new SkillCheckOps(game);
    }

    public LoyaltyDeckOps loyaltyDeck() {
        return new LoyaltyDeckOps(game);
    }

    public TitleOps titles() {
        return new TitleOps(game);
    }

    /**
     * Lets a player other than the current one act (e.g. the target of an "Executive Order" card).
     */
    public void actingPlayer(int playerNumber) {
        game.actingPlayerNumber(playerNumber == game.currentPlayerNumber() ? null : playerNumber);
    }

    /**
     * The current player acts again.
     */
    public void resetActingPlayer() {
        game.actingPlayerNumber(null);
    }

    public int rollDie() {
        return game.die().roll();
    }
}
