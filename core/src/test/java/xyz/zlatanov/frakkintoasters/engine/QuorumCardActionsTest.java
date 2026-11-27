package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.ArrestOrderEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.AssignArbitratorEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.AssignMissionSpecialistEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.EnactProductionQuotasEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.EstablishDogsvilleEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.FoodRationingEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.ResignationEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.DiscardHeldQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.ExecutiveOrderEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.PopularInfluenceChoiceEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.PopularInfluenceEvent;
import xyz.zlatanov.frakkintoasters.event.action.title.PresidentTitleActionEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.jump.DrawDestinationCardsEvent;
import xyz.zlatanov.frakkintoasters.event.location.colonialone.QuorumChamberEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.AdmiralsQuartersEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.GiveQuorumCardToPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.UseArbitratorEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActionStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.QUORUM_CHAMBER;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.POPULAR_INFLUENCE;

/**
 * The President gets his cards with {@link GiveQuorumCardToPresidentEvent}, which a seeded game can't choose.
 */
class QuorumCardActionsTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private static final SkillCard EXECUTIVE_ORDER_1   = new SkillCard(1, EXECUTIVE_ORDER);
    private static final SkillCard POPULAR_INFLUENCE_3 = new SkillCard(3, POPULAR_INFLUENCE);

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

    // The President plays a Quorum Card as one of the actions an "Executive Order" gives him.
    @Test
    void thePresidentPlaysACardOnAGrantedAction() {
        start(all(setUp(WILLIAM_ADAMA), single(new MakePresidentEvent(LAURA_ROSLIN)),
                single(new GiveSkillCardEvent(1, EXECUTIVE_ORDER_1)), single(new GiveQuorumCardToPresidentEvent(ARREST_ORDER)),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new ExecutiveOrderEvent(1, EXECUTIVE_ORDER_1, 2)));
        choose(2, all(new GrantEvent(2, ACTION, GrantedBy.EXECUTIVE_ORDER),
                new GrantEvent(2, ACTION, GrantedBy.EXECUTIVE_ORDER)));
        act(2);
        play(engine -> engine.submit(2, new ArrestOrderEvent(2, 3)));
        pass(2);

        assertEquals(Optional.of(BRIG), engine.view().locate(KARA_STARBUCK_THRACE));
        assertEquals(List.of(ARREST_ORDER), game().decks().quorum().discardedCards());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // The new President takes the hand with the title.
    @Test
    void resignationGivesTheTitleAndTheHandAway() {
        start(all(setUp(WILLIAM_ADAMA), single(new GiveQuorumCardToPresidentEvent(RESIGNATION)),
                single(new GiveQuorumCardToPresidentEvent(EULOGY)), single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new ResignationEvent(1, List.of(EULOGY))));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, MakePresidentEvent.class)));
        play(engine -> engine.submit(1, new MakePresidentEvent(LAURA_ROSLIN)));

        assertEquals(LAURA_ROSLIN, engine.view().president());
        assertEquals(1, game().presidentHand().size());
        assertEquals(List.of(EULOGY, RESIGNATION), game().decks().quorum().discardedCards());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // The Arbitrator is asked as the "Admiral's Quarters" is activated, and the card leaves play once used.
    @Test
    void theArbitratorLowersAnAdmiralsQuartersCheck() {
        start(all(setUp(GAIUS_BALTAR_ALT), single(new GiveQuorumCardToPresidentEvent(ASSIGN_ARBITRATOR)),
                single(new MoveCharacterEvent(GAIUS_BALTAR_ALT, ADMIRALS_QUARTERS)), single(new ActionStepEvent()),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new AssignArbitratorEvent(1, 2)));
        act(1);
        play(engine -> engine.submit(1, new AdmiralsQuartersEvent(1, 3)));
        choose(2, single(new UseArbitratorEvent(2, -3)));

        // and Kara Thrace's "Insubordinate" makes it 3 easier against her as she is chosen
        assertEquals(-6, engine.view().activeSkillCheck().difficultyAdjustment());
        assertEquals(List.of(), engine.view().quorumCardsInPlay());
        assertTrue(game().decks().quorum().discardedCards().contains(ASSIGN_ARBITRATOR));
    }

    // At the next jump the Mission Specialist, not the Admiral, chooses from 3 Destination Cards.
    @Test
    void theMissionSpecialistChoosesTheDestination() {
        start(all(setUp(WILLIAM_ADAMA), single(new GiveQuorumCardToPresidentEvent(ASSIGN_MISSION_SPECIALIST)),
                single(new ActionStepEvent()), single(new DrawDestinationCardsEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new AssignMissionSpecialistEvent(1, 3)));

        val choice = (AwaitingChoice) engine.pendingInput().orElseThrow();
        assertEquals(3, choice.player());
        assertEquals(3, choice.options().size());
        assertEquals(List.of(), engine.view().quorumCardsInPlay());
    }

    // A player who isn't President gives one card to the President and discards the other.
    @Test
    void popularInfluenceGivesOneCardAndDiscardsTheOther() {
        start(all(setUp(WILLIAM_ADAMA), single(new MakePresidentEvent(LAURA_ROSLIN)),
                single(new GiveSkillCardEvent(1, POPULAR_INFLUENCE_3)), single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new PopularInfluenceEvent(1, POPULAR_INFLUENCE_3)));
        val choice = (AwaitingChoice) engine.pendingInput().orElseThrow();
        val option = (Followup.Single) choice.options().iterator().next();
        val picked = (PopularInfluenceChoiceEvent) option.event();
        play(engine -> engine.choose(1, option));

        assertTrue(game().presidentHand().cards().contains(picked.given()));
        assertEquals(List.of(picked.other()), engine.view().player(1).quorumCards().cards());
        choose(1, single(new DiscardHeldQuorumCardEvent(1, picked.other())));

        assertEquals(List.of(), engine.view().player(1).quorumCards().cards());
        assertEquals(List.of(picked.other()), game().decks().quorum().discardedCards());
        assertEquals(new Finished(CYLONS), engine.status(1));
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

    private void act(int player) {
        choose(player, single(new PlayerDecisionEvent<>(player, ActionEvent.class)));
    }

    private void pass(int player) {
        choose(player, single(new NoOpEvent(player)));
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
