package xyz.zlatanov.frakkintoasters;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.board.PegasusBoard;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

public abstract class EventProcessor<T extends Event> {

    protected Game game;
    protected T    event;

    //utility fields improving event processors' readability
    protected GalacticaBoard  galacticaBoard;
    protected PegasusBoard    pegasusBoard;
    protected CylonFleetBoard cylonFleetBoard;

    public final Followup execute(Game game, T event) {
        setContext(game, event);
        init();
        validate();
        return process();
    }

    public abstract Followup process();

    protected void init() {
        //allows initialization of helper fields in subclasses with heavy logic
    }

    protected boolean isValid() {
        return true;
    }

    private void setContext(Game game, T event) {
        assert this.game == null && this.event == null;
        this.game = game;
        this.event = event;
        this.galacticaBoard = game.boards().galactica();
        this.pegasusBoard = game.boards().pegasus();
        this.cylonFleetBoard = game.boards().cylonFleet();
    }

    private void validate() {
        if (!isValid()) {
            throw new InvalidActionException("Invalid action!");
        }
    }

    //todo maybe instead of utility methods use utility fields for a lot of stuff?
    protected Player player() {
        if (event instanceof PlayerEvent playerEvent) {
            return game.player(playerEvent.playerNumber());
        } else {
            throw new FrakCallTheAdmiralException("Cannot find player for non-player event");
        }
    }

    protected int rollDie() {
        return game.die().roll();
    }

}
