package xyz.zlatanov.frakkintoasters;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.view.*;

public abstract class EventProcessor<T extends Event> {

    protected GameView game;
    protected T        event;
    protected GameOps  ops; //set right before process(), so init() and isValid() can't change state

    //utility fields improving event processors' readability
    protected PlayerView          player;
    protected GalacticaBoardView  galacticaBoard;
    protected PegasusBoardView    pegasusBoard;
    protected CylonFleetBoardView cylonFleetBoard;

    public final Followup execute(Game game, T event) {
        setContext(game, event);
        init();
        validate();
        ops = new GameOps(game);
        return process();
    }

    public abstract Followup process();

    protected void init() { //todo probably remove
        //allows initialization of helper fields in subclasses with heavy logic
    }

    protected boolean isValid() {
        return true;
    }

    private void setContext(GameView game, T event) {
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

}
