package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;

public class GameOps extends AbstractOps {

    public GameOps(Game game) {
        super(game);
    }

    public PlayerOps player(int playerNumber) {
        return new PlayerOps(game, playerNumber);
    }

    public CharacterOps character(Character character) {
        return new CharacterOps(game, character);
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

    public DestinyDeckOps destinyDeck() {
        return new DestinyDeckOps(game);
    }

    public PresidentOps president() {
        return new PresidentOps(game);
    }

    public AdmiralOps admiral() {
        return new AdmiralOps(game);
    }

    public CagOps cag() {
        return new CagOps(game);
    }

    public int rollDie() {
        return game.die().roll();
    }
}
