package xyz.zlatanov.frakkintoasters;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.board.PegasusBoard;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

public abstract class EventProcessor<T extends Event> {

    protected Game game; //todo replace with GameView
    protected T    event;

    //utility fields improving event processors' readability
    protected PlayerView      player;
    protected GalacticaBoard  galacticaBoard; //todo replace with board view
    protected PegasusBoard    pegasusBoard;//todo replace with board view
    protected CylonFleetBoard cylonFleetBoard;//todo replace with board view

    public final Followup execute(Game game, T event) {
        setContext(game, event);
        init();
        validate();
        initOperations(game);
        return process();
    }

    public abstract Followup process();

    protected void init() { //todo probably remove
        //allows initialization of helper fields in subclasses with heavy logic
    }

    protected boolean isValid() {
        return true;
    }

    protected void initOperations(Game game) {
        //allows GameOperation initialization before processing the event's business logic
    }

    private void setContext(Game game, T event) {
        assert this.game == null && this.event == null;
        this.game = game;
        this.event = event;
        this.galacticaBoard = game.boards().galactica();
        this.pegasusBoard = game.boards().pegasus();
        this.cylonFleetBoard = game.boards().cylonFleet();
        if (event instanceof PlayerEvent playerEvent) {
            this.player = game.player(playerEvent.playerNumber());
        }
    }

    private void validate() {
        if (!isValid()) {
            throw new InvalidActionException("Invalid action!");
        }
    }

    //utility methods
    protected int rollDie() {
        return game.die().roll();
    }

}
