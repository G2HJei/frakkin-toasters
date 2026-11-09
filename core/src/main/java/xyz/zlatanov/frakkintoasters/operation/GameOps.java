package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.OwnTurnStarted;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.*;

public class GameOps extends AbstractOps {

    private static final List<DeckId> STARTING_DECKS = List.of(CIVILIAN_SHIPS, GALACTICA_DAMAGE, PEGASUS_DAMAGE, BASESTAR_DAMAGE,
            DESTINATION, POLITICS, LEADERSHIP, TACTICS, PILOTING, ENGINEERING, TREACHERY, QUORUM, CRISIS, SUPER_CRISIS, MUTINY, MOTIVE);

    public GameOps(Game game, FactRecorder recorder) {
        super(game, recorder);
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
        return new PlayerOps(game, recorder, playerNumber);
    }

    public ShipOps ship(int shipId) {
        return new ShipOps(game, recorder, shipId);
    }

    public GalacticaOps galactica() {
        return new GalacticaOps(game, recorder);
    }

    public PegasusOps pegasus() {
        return new PegasusOps(game, recorder);
    }

    public CylonFleetOps cylonFleet() {
        return new CylonFleetOps(game, recorder);
    }

    public SkillCheckOps skillCheck() {
        return new SkillCheckOps(game, recorder);
    }

    public LoyaltyDeckOps loyaltyDeck() {
        return new LoyaltyDeckOps(game, recorder);
    }

    public TitleOps titles() {
        return new TitleOps(game, recorder);
    }

    /**
     * Lets a player other than the current one act (e.g. the target of an "Executive Order" card).
     */
    public void actingPlayer(int playerNumber) {
        record(new ActingPlayerChanged(playerNumber == game.turn().currentPlayerNumber() ? null : playerNumber));
    }

    /**
     * The current player acts again.
     */
    public void resetActingPlayer() {
        record(new ActingPlayerChanged(null));
    }

    /**
     * Starts the journal and shuffles the decks that exist from the start. Destiny and loyalty decks are created later.
     */
    public void startGame() {
        record(new GameStarted(game.players().size(), game.objective()));
        STARTING_DECKS.forEach(this::shuffle);
    }

    public void shuffleDeck(DeckId id) {
        shuffle(id);
    }

    public void changeStep(GameStep step) {
        record(new StepChanged(step));
    }

    public void startTurn() {
        record(new TurnStarted(game.turn().number() + 1));
        record(new OwnTurnStarted(game.turn().currentPlayerNumber()));
    }

    public void changeCurrentPlayer(int playerNumber) {
        record(new CurrentPlayerChanged(playerNumber));
    }

    /**
     * @return the first player
     */
    public int chooseFirstPlayer() {
        // The current player token is a random player(rule A3 .7 .1).
        int first = game.randomness().nextInt(1, game.players().size() + 1);
        changeCurrentPlayer(first);
        return first;
    }

    public int rollDie() {
        int value = game.die().roll();
        record(new DieRolled(value));
        return value;
    }
}
