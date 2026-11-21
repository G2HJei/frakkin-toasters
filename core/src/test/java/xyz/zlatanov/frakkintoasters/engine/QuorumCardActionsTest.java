package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.EnactProductionQuotasEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.EstablishDogsvilleEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.FoodRationingEvent;
import xyz.zlatanov.frakkintoasters.event.action.title.PresidentTitleActionEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.location.colonialone.QuorumChamberEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.GiveQuorumCardToPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActionStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.QUORUM_CHAMBER;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

/**
 * Quorum Cards played through {@code submit} and {@code choose} alone, restoring the game at every pause. The President
 * gets his cards with {@link GiveQuorumCardToPresidentEvent}, which a seeded game can't choose.
 */
class QuorumCardActionsTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private GameEngine engine;

    @Test
    void thePresidentPlaysACardAsHisAction() {
        start(all(setUp(WILLIAM_ADAMA), single(new GiveQuorumCardToPresidentEvent(ENACT_PRODUCTION_QUOTAS)),
                single(new ActionStepEvent()), CYLONS_WIN));
        val food = galactica().food();
        val morale = galactica().morale();

        act(1);
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, ActionEvent.class)));
        play(engine -> engine.submit(1, new EnactProductionQuotasEvent(1)));

        assertEquals(food + 1, galactica().food());
        assertEquals(morale - 1, galactica().morale());
        assertEquals(List.of(), game().presidentHand().cards());
        assertEquals(List.of(ENACT_PRODUCTION_QUOTAS), game().decks().quorum().discardedCards());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    @Test
    void foodRationingLeavesTheGameOnSixOrMore() {
        start(all(setUp(WILLIAM_ADAMA), single(new GiveQuorumCardToPresidentEvent(FOOD_RATIONING)),
                single(new ActionStepEvent()), CYLONS_WIN));
        val food = galactica().food();

        act(1);
        play(engine -> engine.submit(1, new FoodRationingEvent(1)));

        // the seed decides the roll
        if (engine.view().turn().dieRoll().result() >= 6) {
            assertEquals(food + 1, galactica().food());
            assertTrue(game().removedComponents().contains(FOOD_RATIONING));
        } else {
            assertEquals(food, galactica().food());
            assertEquals(List.of(FOOD_RATIONING), game().decks().quorum().discardedCards());
        }
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    @Test
    void thePresidentTitleCardDrawsAQuorumCard() {
        start(all(setUp(WILLIAM_ADAMA), single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new PresidentTitleActionEvent(1)));

        assertEquals(1, game().presidentHand().size());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // The Quorum Chamber draws a card, and then the President plays one.
    @Test
    void theQuorumChamberPlaysACard() {
        start(all(setUp(WILLIAM_ADAMA), single(new MoveCharacterEvent(WILLIAM_ADAMA, QUORUM_CHAMBER)),
                single(new GiveQuorumCardToPresidentEvent(ESTABLISH_DOGSVILLE)), single(new ActionStepEvent()),
                CYLONS_WIN));
        val population = galactica().population();

        act(1);
        play(engine -> engine.submit(1, new QuorumChamberEvent(1)));
        assertEquals(2, game().presidentHand().size());
        choose(1, single(new PlayerDecisionEvent<>(1, QuorumCardEvent.class)));
        play(engine -> engine.submit(1, new EstablishDogsvilleEvent(1)));

        assertEquals(population + 1, galactica().population());
        assertEquals(1, game().presidentHand().size());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    @Test
    void onlyThePresidentPlaysQuorumCards() {
        start(all(setUp(LAURA_ROSLIN), single(new MakePresidentEvent(WILLIAM_ADAMA)),
                single(new GiveQuorumCardToPresidentEvent(ENACT_PRODUCTION_QUOTAS)), single(new ActionStepEvent()),
                CYLONS_WIN));

        act(1);

        assertThrows(InvalidActionException.class, () -> engine.submit(1, new EnactProductionQuotasEvent(1)));
    }

    // The first player's character becomes President.
    private static Followup setUp(Character first) {
        return all(
                new SelectCharacterEvent(1, first),
                new SelectCharacterEvent(2, first == LAURA_ROSLIN ? WILLIAM_ADAMA : LAURA_ROSLIN),
                new SelectCharacterEvent(3, KARA_STARBUCK_THRACE),
                new MakePresidentEvent(first));
    }

    private void start(Followup script) {
        engine = new GameEngine(3, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    // The player chooses to take an action.
    private void act(int player) {
        choose(player, single(new PlayerDecisionEvent<>(player, ActionEvent.class)));
    }

    private void choose(int player, Followup option) {
        val choice = (AwaitingChoice) engine.pendingInput().orElseThrow();
        assertTrue(choice.options().contains(option), choice.options().toString());
        play(engine -> engine.choose(player, option));
    }

    private Game game() {
        return (Game) engine.view();
    }

    private xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView galactica() {
        return engine.view().boards().galactica();
    }

    /* assertions */

    private void assertWaitingFor(PendingInput expected) {
        assertEquals(EngineStatus.waiting(expected), engine.status(expected.player()));
    }

    private void assertRestoresTheSameGame() {
        val restored = GameEngine.restore(engine.journal());

        assertEquals(snapshot(engine), snapshot(restored));
        assertEquals(statuses(engine), statuses(restored));
    }

    // The view is the game itself.
    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
