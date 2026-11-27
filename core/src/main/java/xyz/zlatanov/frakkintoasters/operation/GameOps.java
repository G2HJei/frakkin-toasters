package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsDiscarded;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.OwnTurnStarted;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.TurnLimit;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
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

    public void actingPlayer(int playerNumber) {
        record(new ActingPlayerChanged(playerNumber == game.turn().currentPlayerNumber() ? null : playerNumber));
    }

    public void allowRemoteAction(int playerNumber, Location location) {
        record(new RemoteActionAllowed(playerNumber, location));
    }

    public void endRemoteAction() {
        if (game.players().stream().anyMatch(p -> game.turn().remoteActionLocation(p.number()).isPresent())) {
            record(new RemoteActionEnded());
        }
    }

    public void resetActingPlayer() {
        record(new ActingPlayerChanged(null));
    }

    /**
     * Destiny and loyalty decks are created later.
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

    /**
     * Takes a Crisis Card that was just revealed out of the turn and discards it, e.g. for "Secret Destiny". Its icons no
     * longer count.
     */
    public void discardRevealedCrisisCard(CrisisCard card) {
        record(new CrisisCardsCleared(List.of(card)));
        record(new CardsDiscarded(CRISIS, List.of(card)));
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

    public void discardPlayedMutinyCard(MutinyCard card) {
        record(new CardsDiscarded(MUTINY, List.of(card)));
    }

    public void removeMutinyCardFromGame(MutinyCard card) {
        record(new DeckFact.CardsRemovedFromGame(MUTINY, List.of(card)));
    }

    public void discardSuperCrisisCard(SuperCrisisCard card) {
        record(new CardsDiscarded(SUPER_CRISIS, List.of(card)));
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

    public void useTurnLimit(int playerNumber, TurnLimit limit) {
        record(new TurnLimitUsed(playerNumber, limit));
    }

    /**
     * Notes where the current player is at the start of his turn, for an ability that compares it later, e.g. "Stim
     * Junkie".
     */
    public void noteStartLocation(Location location) {
        record(new StartLocationNoted(location));
    }

    public void sendToBrigAtEndOfTurn(int playerNumber) {
        record(new SentToBrigAtEndOfTurn(playerNumber));
    }

    public void payActionCost(int playerNumber) {
        record(new ActionCostPaid(playerNumber));
    }

    public void actionCostSettled() {
        record(new ActionCostPaid(null));
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

    public int chooseFirstPlayer() {
        // The current player token is a random player (rule A3.7.1).
        int first = game.randomness().nextInt(1, game.players().size() + 1);
        changeCurrentPlayer(first);
        return first;
    }

    public void endGame(Team winner, List<Integer> winners) {
        record(new GameEnded(winner, winners));
    }

    public void startDieRoll() {
        record(new DieRollStarted());
    }

    /**
     * Only {@code RollDieEventProcessor} calls it, so no roll skips the interrupt windows around it.
     */
    public int rollDie() {
        int value = game.die().roll();
        record(new DieRolled(value));
        return value;
    }

    public void playOnDieRoll(SkillCardType card, int modifier) {
        record(new DieRollCardPlayed(card, modifier));
    }

    public void useAbilityOnDieRoll(CharacterAbility ability) {
        record(new DieRollAbilityUsed(ability));
    }

    /**
     * An ability sets the face instead of the die ("Hotshot"), and a fixed one is the result as it is ("Longshot").
     */
    public void replaceDieRoll(int value, boolean fixed) {
        record(new DieRollReplaced(value, fixed));
    }
}
