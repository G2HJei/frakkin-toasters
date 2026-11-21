package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.InterruptFact.InterruptWindowOpened;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.PlayInterruptCardEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.RollDieEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.TiebreakInterruptCardsEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.result.KeepTabsOnVisitorFailEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

/**
 * Interrupt windows played through {@code submit} alone, restoring the game at every pause. "Keep Tabs on Visitor" fails
 * with a die roll: "If 4 or lower, -2 population."
 */
class InterruptWindowTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private static final SkillCard STRATEGIC_PLANNING_3 = new SkillCard(3, STRATEGIC_PLANNING);
    private static final SkillCard STRATEGIC_PLANNING_4 = new SkillCard(4, STRATEGIC_PLANNING);
    private static final SkillCard EVASIVE_MANOEUVRES_1 = new SkillCard(1, EVASIVE_MANOEUVRES);

    private GameEngine engine;

    @Test
    void aWindowWithoutHoldersDoesNotPause() {
        start(all(setUp(), single(new KeepTabsOnVisitorFailEvent()), CYLONS_WIN));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(2, count(scopesOpened(), OpenInterruptWindowEvent.class)); // before and after the roll
        assertTrue(engine.journal().facts().stream().noneMatch(InterruptWindowOpened.class::isInstance));
        assertPopulationAfterKeepTabsOnVisitor();
    }

    // "Play before any die roll to add 2 to the result."
    @Test
    void aHeldCardPausesTheGameAtItsWindowInSecret() {
        start(all(setUp(), single(new GiveSkillCardEvent(2, STRATEGIC_PLANNING_3)), single(new KeepTabsOnVisitorFailEvent()),
                CYLONS_WIN));

        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(2, PlayInterruptCardEvent.class)));
        assertEquals(EngineStatus.waiting(), engine.status(1)); // the others don't learn who holds a card
        assertEquals(EngineStatus.waiting(), engine.status(3));

        play(engine -> engine.submit(2, PlayInterruptCardEvent.play(2, STRATEGIC_PLANNING_3)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(List.of(STRATEGIC_PLANNING), engine.view().turn().dieRoll().cards());
        assertEquals(2, engine.view().turn().dieRoll().modifier());
        assertEquals(List.of(), engine.view().player(2).skillCards().cards());
        assertPopulationAfterKeepTabsOnVisitor();
    }

    @Test
    void aPassIsAnAnswerToo() {
        start(all(setUp(), single(new GiveSkillCardEvent(2, STRATEGIC_PLANNING_3)), single(new KeepTabsOnVisitorFailEvent()),
                CYLONS_WIN));

        play(engine -> engine.submit(2, PlayInterruptCardEvent.pass(2)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(List.of(), engine.view().turn().dieRoll().cards());
        assertEquals(List.of(STRATEGIC_PLANNING_3), engine.view().player(2).skillCards().cards());
    }

    // "Limit of 1 "Strategic Planning" card used per die roll": the current player decides which one stands, and the
    // other goes back to its owner's hand (rule 13.3). The game is restored inside the window and goes on from there.
    @Test
    void twoStrategicPlanningCardsGoToTheCurrentPlayer() {
        start(all(setUp(),
                all(new GiveSkillCardEvent(2, STRATEGIC_PLANNING_3), new GiveSkillCardEvent(3, STRATEGIC_PLANNING_4)),
                single(new KeepTabsOnVisitorFailEvent()),
                CYLONS_WIN));

        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(2, PlayInterruptCardEvent.class)));
        play(engine -> engine.submit(2, PlayInterruptCardEvent.play(2, STRATEGIC_PLANNING_3)));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(3, PlayInterruptCardEvent.class)));
        play(engine -> engine.submit(3, PlayInterruptCardEvent.play(3, STRATEGIC_PLANNING_4)));

        // The current player sees the two cards as his options.
        val kept2 = single(new TiebreakInterruptCardsEvent(new PlayedInterruptCard(2, STRATEGIC_PLANNING_3, null)));
        val kept3 = single(new TiebreakInterruptCardsEvent(new PlayedInterruptCard(3, STRATEGIC_PLANNING_4, null)));
        assertWaitingFor(new AwaitingChoice(1, new LinkedHashSet<>(List.of(kept2, kept3))));
        engine = GameEngine.restore(engine.journal());
        play(engine -> engine.choose(1, kept3));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(List.of(STRATEGIC_PLANNING), engine.view().turn().dieRoll().cards());
        assertEquals(2, engine.view().turn().dieRoll().modifier());
        assertEquals(List.of(STRATEGIC_PLANNING_3), engine.view().player(2).skillCards().cards());
        assertEquals(List.of(), engine.view().player(3).skillCards().cards());
    }

    // "Evasive Maneuvers": "Play after any viper is attacked to reroll the die." The window opens again on the new roll,
    // and nobody holds a card for it any more.
    @Test
    void evasiveManoeuvresRerollsARaidersAttack() {
        start(all(setUp(),
                all(new GiveSkillCardEvent(2, EVASIVE_MANOEUVRES_1),
                        new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_4_OCLOCK),
                        new PlaceRaiderEvent(GALACTICA_SPACE_4_OCLOCK),
                        new ActivateRaidersEvent()),
                CYLONS_WIN));

        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(2, PlayInterruptCardEvent.class)));
        assertEquals(1, count(scopesOpened(), RollDieEvent.class));

        play(engine -> engine.submit(2, PlayInterruptCardEvent.play(2, EVASIVE_MANOEUVRES_1)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(2, count(scopesOpened(), RollDieEvent.class));
        assertEquals(List.of(EVASIVE_MANOEUVRES), engine.view().turn().dieRoll().cards());
        assertEquals(0, engine.view().turn().dieRoll().modifier()); // the viper is unmanned
    }

    @Test
    void anAnswerNamesACardForTheWindow() {
        start(all(setUp(), single(new GiveSkillCardEvent(2, STRATEGIC_PLANNING_3)), single(new KeepTabsOnVisitorFailEvent()),
                CYLONS_WIN));

        assertThrows(InvalidActionException.class,
                () -> engine.submit(2, PlayInterruptCardEvent.play(2, EVASIVE_MANOEUVRES_1)));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(2, PlayInterruptCardEvent.class)));
    }

    /* the game */

    // William Adama, Laura Roslin and Kara Thrace, with empty hands. Player 1 is the current player.
    private static Followup setUp() {
        return all(
                new SelectCharacterEvent(1, WILLIAM_ADAMA),
                new SelectCharacterEvent(2, LAURA_ROSLIN),
                new SelectCharacterEvent(3, KARA_STARBUCK_THRACE),
                new CreateDestinyDeckEvent());
    }

    private void start(Followup script) {
        engine = new GameEngine(3, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    /* assertions */

    private void assertPopulationAfterKeepTabsOnVisitor() {
        val lost = engine.view().turn().dieRoll().result() <= 4 ? 2 : 0;
        assertEquals(12 - lost, engine.view().boards().galactica().population());
    }

    private void assertWaitingFor(PendingInput expected) {
        assertEquals(EngineStatus.waiting(expected), engine.status(expected.player()));
    }

    private void assertRestoresTheSameGame() {
        val restored = GameEngine.restore(engine.journal());

        assertEquals(snapshot(engine), snapshot(restored));
        assertEquals(statuses(engine), statuses(restored));
    }

    private List<Event> scopesOpened() {
        return engine.journal().entries().stream()
                .filter(ScopeOpened.class::isInstance)
                .map(entry -> ((ScopeOpened) entry).event())
                .toList();
    }

    private static long count(List<Event> events, Class<?> type) {
        return events.stream().filter(type::isInstance).count();
    }

    // The view is the game itself.
    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
