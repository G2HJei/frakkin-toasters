package xyz.zlatanov.frakkintoasters;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.*;

import java.util.ArrayList;
import java.util.List;

public abstract class EventProcessor<T extends Event> {

    protected GameView game;
    protected T        event;
    protected GameOps  ops; //set by run(), so init() and isValid() can't change state

    //utility fields improving event processors' readability
    protected PlayerView          player;
    protected GalacticaBoardView  galacticaBoard;
    protected PegasusBoardView    pegasusBoard;
    protected CylonFleetBoardView cylonFleetBoard;

    private boolean prepared;

    /**
     * Sets the context and validates the event. It has no ops, so a rejected event changes nothing.
     *
     * @param game the game the event runs in
     * @return this processor, for {@link #run}
     * @throws InvalidActionException if the event isn't valid
     */
    public final EventProcessor<T> prepare(GameView game, T event) {
        setContext(game, event);
        init();
        validate();
        prepared = true;
        return this;
    }

    /**
     * Processes the event that {@link #prepare} accepted.
     *
     * @param ops game ops bound to the game {@link #prepare} got
     * @return what the event queued
     */
    public final Followup run(GameOps ops) {
        if (!prepared || this.ops != null) {
            throw new FrakCallTheAdmiralException();
        }
        this.ops = ops;
        return process();
    }

    public abstract Followup process();

    protected void init() { //todo probably remove
        //allows initialization of helper fields in subclasses with heavy logic
    }

    protected boolean isValid() {
        return true;
    }

    // A player number sent by a client may name nobody.
    protected boolean isPlayer(Integer playerNumber) {
        return playerNumber != null && playerNumber >= 1 && game.players().size() >= playerNumber;
    }

    // Each card takes one copy out of the player's hand, because the decks hold equal cards (8 "Executive Order" 1s).
    protected boolean holdsSkillCards(List<SkillCard> cards) {
        val hand = new ArrayList<>(player.skillCards().cards());
        return cards != null && cards.stream().allMatch(hand::remove);
    }

    private void setContext(GameView game, T event) {
        if (this.game != null) {
            throw new FrakCallTheAdmiralException();
        }
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
