package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.board.PegasusBoard;
import xyz.zlatanov.frakkintoasters.state.ship.*;

public class EventTest {

    public Game game = Game.builder().build();

    public GalacticaBoard  galacticaBoard  = game.boards().galactica();
    public PegasusBoard    pegasusBoard    = game.boards().pegasus();
    public CylonFleetBoard cylonFleetBoard = game.boards().cylonFleet();

    public CylonShips cylonShips = game.cylonShips();

    public void setGame(Game game) {
        this.game = game;
        this.galacticaBoard = game.boards().galactica();
        this.pegasusBoard = game.boards().pegasus();
        this.cylonFleetBoard = game.boards().cylonFleet();
        this.cylonShips = game.cylonShips();
    }

    public Basestar basestar() {
        return cylonShips.basestar().orElseThrow();
    }

    public Raider raider() {
        return cylonShips.raider().orElseThrow();
    }

    public HeavyRaider heavyRaider() {
        return cylonShips.heavyRaider().orElseThrow();
    }

    public Centurion centurion() {
        return cylonShips.centurion().orElseThrow();
    }
}
