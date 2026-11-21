package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsDiscarded;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.OwnTurnStarted;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

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

    public InterruptOps interrupts() {
        return new InterruptOps(game, recorder);
    }

    public SandboxOps sandbox() {
        return new SandboxOps(game, recorder);
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

    /**
     * Ends the turn before its steps do, e.g. when the current player was executed (rule 7.1).
     */
    public void endTurnEarly() {
        record(new TurnEndedEarly());
    }

    public void changeCurrentPlayer(int playerNumber) {
        record(new CurrentPlayerChanged(playerNumber));
    }

    public void playCrisisCard(CrisisCard card) {
        record(new CrisisCardPlayed(card));
    }

    public void discardCrisisCards() {
        val cards = game.turn().crisisCards();
        if (cards.isEmpty()) {
            return;
        }
        record(new CrisisCardsCleared(cards));
        // "Keep this card in play": the cards kept stay out of the discard pile.
        val discarded = cards.stream()
                .filter(card -> card.skillCheck() == null || !game.crisisCardsInPlay().contains(card.skillCheck()))
                .toList();
        if (!discarded.isEmpty()) {
            record(new CardsDiscarded(CRISIS, discarded));
        }
    }

    public void keepCrisisCardInPlay(SkillCheckType type) {
        record(new CrisisCardKeptInPlay(type));
    }

    /**
     * "After the Activate Cylon Ships step, return to the Resolve Crisis step."
     */
    public void returnToCrisisStep(boolean returns) {
        record(new ReturnToCrisisStepChanged(returns));
    }

    public void discardNuke() {
        record(new NukeDiscarded());
    }

    public void gainNuke() {
        record(new NukeGained());
    }

    /**
     * "Negotiation": no Cylon ship launches or activates for the rest of the turn.
     */
    public void holdCylonShips() {
        record(new CylonShipsHeld());
    }

    public void moraleLostToExecution() {
        record(new MoraleLostToExecution());
    }

    /**
     * "Preventative Policy": the next loss of the resource this turn is 1 smaller.
     */
    public void reduceNextLoss(Resource resource) {
        record(new LossReductionAdded(resource));
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

    public void endGame(Team winner, List<Integer> winners) {
        record(new GameEnded(winner, winners));
    }

    /**
     * Starts a new die roll, before the cards played on it. The window before the roll does it.
     */
    public void startDieRoll() {
        record(new DieRollStarted());
    }

    /**
     * Rolls the die of the roll that was started, or rerolls it. Only {@code RollDieEventProcessor} calls it, so no roll
     * skips the interrupt windows around it.
     */
    public int rollDie() {
        int value = game.die().roll();
        record(new DieRolled(value));
        return value;
    }

    public void playOnDieRoll(SkillCardType card, int modifier) {
        record(new DieRollCardPlayed(card, modifier));
    }
}
