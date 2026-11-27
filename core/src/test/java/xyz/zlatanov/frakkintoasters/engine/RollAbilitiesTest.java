package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.DieRolled;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.PlayInterruptCardEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.RollDieEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.UseRollAbilityEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.result.KeepTabsOnVisitorFailEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActionStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterFtlControlRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeDieRoll;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CALCULATIONS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

/**
 * "Keep Tabs on Visitor" fails with a die roll: on 4 or lower, -2 population. Player 1 is the current player.
 */
class RollAbilitiesTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private static final SkillCard CALCULATIONS_1 = new SkillCard(1, CALCULATIONS);

    private GameEngine engine;

    // Helo, on his turn, rerolls the die just rolled. The window opens again on the new roll, where he can't any more.
    @Test
    void ecoOfficerRerollsADieOnHisTurn() {
        start(all(setUp(KARL_HELO_AGATHON, LAURA_ROSLIN, KARA_STARBUCK_THRACE), single(new KeepTabsOnVisitorFailEvent()),
                CYLONS_WIN));

        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, UseRollAbilityEvent.class)));
        assertEquals(EngineStatus.waiting(), engine.status(2)); // the others don't learn who answers what

        play(engine -> engine.submit(1, UseRollAbilityEvent.use(1, ECO_OFFICER)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(2, count(scopesOpened(), RollDieEvent.class));
        assertEquals(List.of(ECO_OFFICER), engine.view().turn().dieRoll().abilities());
        assertTrue(engine.view().turn().hasUsed(1, ECO_OFFICER));
        assertPopulationAfterKeepTabsOnVisitor();
    }

    // He holds a "Calculations" too: he is asked for the card, then for the ability. The card counts on the new roll.
    @Test
    void aPlayerIsAskedForACardThenForAnAbility() {
        start(all(setUp(KARL_HELO_AGATHON, LAURA_ROSLIN, KARA_STARBUCK_THRACE),
                single(new GiveSkillCardEvent(1, CALCULATIONS_1)),
                single(new KeepTabsOnVisitorFailEvent()),
                CYLONS_WIN));

        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, PlayInterruptCardEvent.class)));
        play(engine -> engine.submit(1, new PlayInterruptCardEvent(1, CALCULATIONS_1, 1)));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, UseRollAbilityEvent.class)));
        assertEquals(EngineStatus.waiting(), engine.status(3));
        play(engine -> engine.submit(1, UseRollAbilityEvent.use(1, ECO_OFFICER)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        val dieRoll = engine.view().turn().dieRoll();
        assertEquals(List.of(CALCULATIONS), dieRoll.cards());
        assertEquals(List.of(ECO_OFFICER), dieRoll.abilities());
        assertEquals(Math.clamp(dieRoll.value() + 1, 1, 8), dieRoll.result());
        assertEquals(2, count(scopesOpened(), RollDieEvent.class));
        assertPopulationAfterKeepTabsOnVisitor();
    }

    @Test
    void anAbilityAnswerIsChecked() {
        start(all(setUp(SAMUEL_T_ANDERS, LAURA_ROSLIN, WILLIAM_ADAMA), single(new KeepTabsOnVisitorFailEvent()),
                CYLONS_WIN));

        assertThrows(InvalidActionException.class,
                () -> engine.submit(1, new UseRollAbilityEvent(1, LONGSHOT, null, 9)));
        assertThrows(InvalidActionException.class, () -> engine.submit(1, UseRollAbilityEvent.use(1, ECO_OFFICER)));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, UseRollAbilityEvent.class)));
    }

    // Anders chooses the result instead of rolling. Nothing modifies it, so no window opens after it, although Roslin
    // holds a "Calculations".
    @Test
    void longshotChoosesTheResultOfARollOnHisTurn() {
        start(all(setUp(SAMUEL_T_ANDERS, LAURA_ROSLIN, WILLIAM_ADAMA),
                single(new GiveSkillCardEvent(2, CALCULATIONS_1)),
                single(new KeepTabsOnVisitorFailEvent()),
                CYLONS_WIN));

        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, UseRollAbilityEvent.class)));
        play(engine -> engine.submit(1, new UseRollAbilityEvent(1, LONGSHOT, null, 5)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        val dieRoll = engine.view().turn().dieRoll();
        assertEquals(5, dieRoll.result());
        assertTrue(dieRoll.fixed());
        assertTrue(engine.journal().facts().stream().noneMatch(DieRolled.class::isInstance));
        assertEquals(List.of(CALCULATIONS_1), engine.view().player(2).skillCards().cards());
        assertEquals(0, engine.view().player(1).miracleTokens());
        assertEquals(12, engine.view().boards().galactica().population());
    }

    // Gaeta rerolls the roll of "FTL Control", whoever's turn it is, once on that roll.
    @Test
    void ftlOperatorRerollsTheRollOfFtlControl() {
        start(all(setUp(LAURA_ROSLIN, FELIX_GAETA, KARA_STARBUCK_THRACE),
                all(new OpenInterruptWindowEvent(new BeforeDieRoll()), new RollDieEvent(),
                        new OpenInterruptWindowEvent(new AfterFtlControlRoll())),
                CYLONS_WIN));

        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(2, UseRollAbilityEvent.class)));
        play(engine -> engine.submit(2, UseRollAbilityEvent.use(2, FTL_OPERATOR)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(2, engine.journal().facts().stream().filter(DieRolled.class::isInstance).count());
        assertEquals(List.of(FTL_OPERATOR), engine.view().turn().dieRoll().abilities());
    }

    // In her Action step, Kat uses a discarded card's strength +2 instead of the roll: 2 + 2 is 4, so -2 population.
    @Test
    void hotshotUsesACardsStrengthInHerActionStep() {
        val discarded = new SkillCard(2, CONSOLIDATE_POWER);
        start(all(setUp(LOUANNE_KAT_KATRAINE, LAURA_ROSLIN, WILLIAM_ADAMA),
                single(new GiveSkillCardEvent(1, discarded)),
                single(new ActionStepEvent()),
                single(new KeepTabsOnVisitorFailEvent()),
                CYLONS_WIN));

        val pass = engine.pendingInput().map(input -> ((AwaitingChoice) input).options().stream()
                        .filter(option -> option.equals(single(new NoOpEvent(1))))
                        .findFirst().orElseThrow())
                .orElseThrow();
        play(engine -> engine.choose(1, pass));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, UseRollAbilityEvent.class)));
        play(engine -> engine.submit(1, new UseRollAbilityEvent(1, HOTSHOT, discarded, null)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(4, engine.view().turn().dieRoll().result());
        assertTrue(engine.journal().facts().stream().noneMatch(DieRolled.class::isInstance));
        assertEquals(List.of(), engine.view().player(1).skillCards().cards());
        assertEquals(10, engine.view().boards().galactica().population());
    }

    private static Followup setUp(Character first, Character second, Character third) {
        return all(
                new SelectCharacterEvent(1, first),
                new SelectCharacterEvent(2, second),
                new SelectCharacterEvent(3, third),
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
