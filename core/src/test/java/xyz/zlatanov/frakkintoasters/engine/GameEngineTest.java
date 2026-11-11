package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ChoiceMade;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.FactRecorded;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeClosed;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.CommandEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.ResearchLabEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.LeaveBrigEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceInSetupLocationEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageGalacticaEvent;
import xyz.zlatanov.frakkintoasters.event.turn.EndTurnEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.engine.journal.Journal.ROOT_SCOPE;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_5;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;

class GameEngineTest {

    private static final PlayerDecisionEvent<SelectCharacterEvent> PLAYER_1_SELECTS =
            new PlayerDecisionEvent<>(1, SelectCharacterEvent.class);
    private static final PlayerDecisionEvent<SelectCharacterEvent> PLAYER_2_SELECTS =
            new PlayerDecisionEvent<>(2, SelectCharacterEvent.class);
    private static final PlayerDecisionEvent<DrawSkillCardsEvent>  PLAYER_2_DRAWS_2 =
            new PlayerDecisionEvent<>(2, DrawSkillCardsEvent.class, DRAW_EXACTLY_2);

    // More than enough to damage all six Galactica locations: the fuel and food tokens go back into the deck.
    private static final int DAMAGE_DRAWS = 30;

    private static final Followup CAIN_SETUP   = one(
            new PlaceInSetupLocationEvent(1, PEGASUS_CIC),
            new PlaceInSetupLocationEvent(1, COMMAND));
    private static final Followup FUEL_OR_FOOD = one(new LoseResourceEvent(FUEL, 1), new LoseResourceEvent(FOOD, 1));
    // Only the end of the game empties the agenda, so a script that plays to the end has to end the game.
    private static final Followup CYLONS_WIN   = single(new CylonsWinEvent());

    // Helena Cain has two setup locations, so selecting her queues a choice, which runs before player 2's decision.
    private static final Followup SCRIPT = all(
            single(PLAYER_1_SELECTS),
            single(PLAYER_2_SELECTS),
            single(PLAYER_2_DRAWS_2),
            FUEL_OR_FOOD,
            single(new NoOpEvent(3)),
            CYLONS_WIN);

    // The answers that play SCRIPT to the end, one per pause.
    private static final List<Consumer<GameEngine>> INPUTS = List.of(
            engine -> engine.submit(1, new SelectCharacterEvent(1, HELENA_CAIN)),
            engine -> engine.choose(1, single(new PlaceInSetupLocationEvent(1, COMMAND))),
            engine -> engine.submit(2, new SelectCharacterEvent(2, LAURA_ROSLIN)),
            engine -> engine.submit(2, new DrawSkillCardsEvent(2, Map.of(POLITICS, 2))),
            engine -> engine.choose(1, single(new LoseResourceEvent(FUEL, 1))));

    private GameEngine engine = start(SCRIPT);

    @Test
    void aScriptRunsThroughSubmitAndChooseAlone() {
        assertWaitingFor(new AwaitingDecision(PLAYER_1_SELECTS));
        engine.submit(1, new SelectCharacterEvent(1, HELENA_CAIN));

        assertWaitingFor(new AwaitingChoice(1, options(CAIN_SETUP)));
        engine.choose(1, single(new PlaceInSetupLocationEvent(1, COMMAND)));
        assertEquals(Optional.of(COMMAND), engine.view().locate(HELENA_CAIN));

        assertWaitingFor(new AwaitingDecision(PLAYER_2_SELECTS));
        engine.submit(2, new SelectCharacterEvent(2, LAURA_ROSLIN));
        assertEquals(Optional.of(PRESIDENTS_OFFICE), engine.view().locate(LAURA_ROSLIN));

        assertWaitingFor(new AwaitingDecision(PLAYER_2_DRAWS_2));
        engine.submit(2, new DrawSkillCardsEvent(2, Map.of(POLITICS, 2)));
        assertEquals(2, engine.view().player(2).skillCards().size());

        // The options name no player, so the current player chooses.
        assertWaitingFor(new AwaitingChoice(1, options(FUEL_OR_FOOD)));
        val fuel = engine.view().boards().galactica().fuel();
        engine.choose(1, single(new LoseResourceEvent(FUEL, 1)));
        assertEquals(fuel - 1, engine.view().boards().galactica().fuel());

        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // A submitted event is logged under the player who submitted it, a queued one under its own player if it has one.
    @Test
    void theJournalNestsEachEventUnderTheEventThatQueuedIt() {
        playScript();

        assertEquals(List.of(
                        new ScopeOpened(1, ROOT_SCOPE, new ScriptEvent(SCRIPT), 1),
                        new ScopeClosed(1, SCRIPT),
                        new ScopeOpened(2, 1, new SelectCharacterEvent(1, HELENA_CAIN), 1),
                        new ScopeClosed(2, CAIN_SETUP),
                        new ChoiceMade(2, 1, single(new PlaceInSetupLocationEvent(1, COMMAND))),
                        new ScopeOpened(3, 2, new PlaceInSetupLocationEvent(1, COMMAND), 1),
                        new ScopeClosed(3, NONE),
                        new ScopeOpened(4, 1, new SelectCharacterEvent(2, LAURA_ROSLIN), 2),
                        new ScopeClosed(4, NONE),
                        new ScopeOpened(5, 1, new DrawSkillCardsEvent(2, Map.of(POLITICS, 2), DRAW_EXACTLY_2), 2),
                        new ScopeClosed(5, NONE),
                        new ChoiceMade(1, 1, single(new LoseResourceEvent(FUEL, 1))),
                        new ScopeOpened(6, 1, new LoseResourceEvent(FUEL, 1), 1),
                        new ScopeClosed(6, NONE),
                        new ScopeOpened(7, 1, new NoOpEvent(3), 3),
                        new ScopeClosed(7, NONE),
                        new ScopeOpened(8, 1, new CylonsWinEvent(), 1),
                        new ScopeClosed(8, NONE)),
                engine.journal().entries().stream().filter(e -> !(e instanceof FactRecorded)).toList());
    }

    @Test
    void everyPlayerSeesAnInputThatIsNotSecret() {
        val expected = EngineStatus.waiting(new AwaitingDecision(PLAYER_1_SELECTS));
        assertEquals(List.of(expected, expected, expected), List.of(engine.status(1), engine.status(2), engine.status(3)));
    }

    /* rejected input */

    @Test
    void onlyThePlayerOfTheDecisionCanAnswerIt() {
        assertRejected(() -> engine.submit(2, new SelectCharacterEvent(2, HELENA_CAIN)));
    }

    @Test
    void aDecisionTakesOnlyItsKindOfEvent() {
        assertRejected(() -> engine.submit(1, new NoOpEvent(1)));
    }

    @Test
    void aPlayerCanSubmitOnlyTheirOwnPlayerEvent() {
        assertRejected(() -> engine.submit(1, new SelectCharacterEvent(3, HELENA_CAIN)));
    }

    @Test
    void anInvalidEventIsRejected() {
        assertRejected(() -> engine.submit(1, new SelectCharacterEvent(1, CAPRICA_SIX))); // no Cylon Leader with 3 players
    }

    @Test
    void aRejectedEventCanBeFollowedByAValidOne() {
        assertRejected(() -> engine.submit(1, new SelectCharacterEvent(1, CAPRICA_SIX)));
        engine.submit(1, new SelectCharacterEvent(1, HELENA_CAIN));
        assertWaitingFor(new AwaitingChoice(1, options(CAIN_SETUP)));
    }

    @Test
    void aDecisionCantBeAnsweredWithAChoice() {
        assertRejected(() -> engine.choose(1, single(new SelectCharacterEvent(1, HELENA_CAIN))));
    }

    @Test
    void aChoiceCantBeAnsweredWithAnEvent() {
        engine.submit(1, new SelectCharacterEvent(1, HELENA_CAIN));
        assertRejected(() -> engine.submit(1, new PlaceInSetupLocationEvent(1, COMMAND)));
    }

    @Test
    void onlyTheChooserCanChoose() {
        engine.submit(1, new SelectCharacterEvent(1, HELENA_CAIN));
        assertRejected(() -> engine.choose(2, single(new PlaceInSetupLocationEvent(1, COMMAND))));
    }

    @Test
    void onlyAnOptionCanBeChosen() {
        engine.submit(1, new SelectCharacterEvent(1, HELENA_CAIN));
        assertRejected(() -> engine.choose(1, single(new PlaceInSetupLocationEvent(1, ADMIRALS_QUARTERS))));
    }

    @Test
    void theConstraintsOfTheDecisionReplaceTheClients() {
        engine.submit(1, new SelectCharacterEvent(1, HELENA_CAIN));
        engine.choose(1, single(new PlaceInSetupLocationEvent(1, COMMAND)));
        engine.submit(2, new SelectCharacterEvent(2, LAURA_ROSLIN));

        val fiveCards = Map.of(POLITICS, 3, LEADERSHIP, 2);
        assertRejected(() -> engine.submit(2, new DrawSkillCardsEvent(2, fiveCards)));
        assertRejected(() -> engine.submit(2, new DrawSkillCardsEvent(2, fiveCards, DRAW_EXACTLY_5)));

        engine.submit(2, new DrawSkillCardsEvent(2, Map.of(POLITICS, 2), DRAW_EXACTLY_5));
        assertEquals(2, engine.view().player(2).skillCards().size());
    }

    @Test
    void aDecisionOnAnInterfaceTakesAnyEventThatImplementsIt() {
        engine = start(all(single(PLAYER_1_SELECTS), single(new PlayerDecisionEvent<>(1, ActionEvent.class)), CYLONS_WIN));
        engine.submit(1, new SelectCharacterEvent(1, GAIUS_BALTAR));

        assertRejected(() -> engine.submit(1, new NoOpEvent(1)));
        assertRejected(() -> engine.submit(1, new CommandEvent(1))); // Gaius Baltar is in the "Research Lab"
        engine.submit(1, new ResearchLabEvent(1, ENGINEERING));

        assertEquals(1, engine.view().player(1).skillCards().size());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    @Test
    void aFinishedGameTakesNoInput() {
        engine = start(CYLONS_WIN);

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertThrows(InvalidActionException.class, () -> engine.submit(1, new NoOpEvent(1)));
        assertThrows(InvalidActionException.class, () -> engine.choose(1, single(new NoOpEvent(1))));
    }

    /* restore */

    @Test
    void restoreGivesTheSameGameAtEveryPause() {
        assertRestoresTheSameGame();
        for (val input : INPUTS) {
            input.accept(engine);
            assertRestoresTheSameGame();
        }
        assertEquals(new Finished(CYLONS), GameEngine.restore(engine.journal()).status(1));
    }

    // The restored journal goes on with the next scope ids, and the original doesn't see what the restored game adds.
    @Test
    void aRestoredGameGoesOnLikeTheOriginal() {
        val fullJournal = start(SCRIPT);
        INPUTS.forEach(input -> input.accept(fullJournal));

        for (int pause = 0; pause < INPUTS.size(); pause++) {
            val restored = GameEngine.restore(engine.journal());
            INPUTS.subList(pause, INPUTS.size()).forEach(input -> input.accept(restored));
            assertEquals(fullJournal.journal().entries(), restored.journal().entries(), "restored at pause " + pause);

            INPUTS.get(pause).accept(engine);
        }
        assertEquals(fullJournal.journal().entries(), engine.journal().entries());
    }

    // The picked option hasn't run yet.
    @Test
    void aJournalThatEndsInTheMiddleOfARunCantBeRestored() {
        playScript();
        val entries = engine.journal().entries();
        val choice = new ChoiceMade(2, 1, single(new PlaceInSetupLocationEvent(1, COMMAND)));
        val upToTheChoice = new Journal(entries.subList(0, entries.indexOf(choice) + 1));

        assertThrows(IllegalArgumentException.class, () -> GameEngine.restore(upToTheChoice));
    }

    /* rollback: a bug after the input was accepted */

    @Test
    void anEventThatThrowsInTheMiddleOfAChainRollsBackTheWholeInput() {
        engine = start(all(single(PLAYER_1_SELECTS), single(new LoseResourceEvent(FOOD, 1)), single(new FailingEvent())));

        val failure = assertRolledBack(() -> engine.submit(1, new SelectCharacterEvent(1, SAUL_TIGH)));

        assertInstanceOf(IllegalStateException.class, failure.getCause());
        assertEquals(Optional.empty(), engine.view().locate(SAUL_TIGH));
    }

    @Test
    void aQueuedEventThatFailsValidationRollsBackTheInput() {
        engine = start(all(single(PLAYER_1_SELECTS), single(new LeaveBrigEvent(1, COMMAND))));

        assertRolledBack(() -> engine.submit(1, new SelectCharacterEvent(1, SAUL_TIGH)));
    }

    @Test
    void anInputThatLeavesNothingToRunBeforeTheGameEndsRollsBack() {
        engine = start(single(PLAYER_1_SELECTS));

        assertRolledBack(() -> engine.submit(1, new SelectCharacterEvent(1, SAUL_TIGH)));
    }

    @Test
    void aChoiceThatRolledBackCanBeAnsweredWithTheOtherOption() {
        val failing = single(new FailingEvent());
        engine = start(all(one(single(new NoOpEvent(1)), failing), CYLONS_WIN));

        assertRolledBack(() -> engine.choose(1, failing));
        engine.choose(1, single(new NoOpEvent(1)));

        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    /* the end of the game */

    // "If six or more locations on Galactica have damage tokens on them at the same time, then Galactica is destroyed and
    // the Cylons immediately win." (rule 16.1)
    @Test
    void aCylonWinEndsTheGameRightAway() {
        destroyGalactica();

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(Optional.of(CYLONS), engine.view().outcome());
        val galactica = engine.view().boards().galactica();
        assertEquals(6, GALACTICA.locations().stream().filter(galactica::isDamaged).count());

        // The damage that destroyed Galactica queued the win, and nothing ran after it: not the other damage, nor
        // player 2's decision.
        val scopes = scopesOpened();
        val win = scopes.getLast();
        val destroyed = scopes.get(scopes.size() - 2);
        assertEquals(new CylonsWinEvent(), win.event());
        assertEquals(new DamageGalacticaEvent(), destroyed.event());
        assertEquals(destroyed.scopeId(), win.parentId());
        assertTrue(scopes.stream().filter(scope -> scope.event() instanceof DamageGalacticaEvent).count() < DAMAGE_DRAWS);
    }

    // The journal records the win with the event that caused it, so restore doesn't run the win checks.
    @Test
    void theWinIsRecordedAsAFollowupOfTheEventThatCausedIt() {
        destroyGalactica();

        val destroyed = scopesOpened().reversed().get(1).scopeId();
        assertTrue(engine.journal().entries().contains(new ScopeClosed(destroyed, single(new CylonsWinEvent()))));
    }

    @Test
    void aWonGameRestoresAsFinished() {
        destroyGalactica();

        assertRestoresTheSameGame();
        assertEquals(new Finished(CYLONS), GameEngine.restore(engine.journal()).status(2));
    }

    @Test
    void aWonGameTakesNoInput() {
        destroyGalactica();

        assertThrows(InvalidActionException.class, () -> engine.submit(2, new SelectCharacterEvent(2, LAURA_ROSLIN)));
    }

    // "If at least one resource (food, fuel, morale, or population) is depleted to 0 at the end of a player's turn, the
    // game immediately ends and the Cylons win." (rule 16.1)
    @Test
    void theCylonsWinWhenAResourceIsDepletedAtTheEndOfATurn() {
        engine = start(all(new LoseResourceEvent(FUEL, 8), new EndTurnEvent()));

        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    /* bugs before the first pause */

    @Test
    void aDecisionCanHaveConstraintsOnlyOnAConstrainedEvent() {
        assertThrows(FrakCallTheAdmiralException.class,
                () -> start(single(new PlayerDecisionEvent<>(1, SelectCharacterEvent.class, DRAW_EXACTLY_2))));
    }

    @Test
    void theAgendaCantRunOutBeforeTheGameEnds() {
        assertThrows(FrakCallTheAdmiralException.class, () -> start(single(new NoOpEvent(1))));
    }

    @Test
    void theOptionsOfAChoiceCantNameDifferentPlayers() {
        assertThrows(FrakCallTheAdmiralException.class, () -> start(one(new NoOpEvent(1), new NoOpEvent(2))));
    }

    private static GameEngine start(Followup script) {
        return new GameEngine(3, Randomness.seeded(42), new ScriptEvent(script));
    }

    private void playScript() {
        INPUTS.forEach(input -> input.accept(engine));
    }

    // Damages Galactica until all six of its locations are damaged, then would ask player 2 to select.
    private void destroyGalactica() {
        val damage = Collections.nCopies(DAMAGE_DRAWS, new DamageGalacticaEvent()).toArray(Event[]::new);
        engine = start(all(single(PLAYER_1_SELECTS), all(damage), single(PLAYER_2_SELECTS)));
        engine.submit(1, new SelectCharacterEvent(1, SAUL_TIGH));
    }

    private List<ScopeOpened> scopesOpened() {
        return engine.journal().entries().stream()
                .filter(ScopeOpened.class::isInstance)
                .map(ScopeOpened.class::cast)
                .toList();
    }

    private void assertWaitingFor(PendingInput expected) {
        assertEquals(EngineStatus.waiting(expected), engine.status(1));
    }

    private void assertRestoresTheSameGame() {
        val restored = GameEngine.restore(engine.journal());

        assertEquals(snapshot(engine), snapshot(restored));
        assertEquals(statuses(engine), statuses(restored));
    }

    private void assertRejected(Executable input) {
        assertNothingChanges(InvalidActionException.class, input);
    }

    private FrakCallTheAdmiralException assertRolledBack(Executable input) {
        return assertNothingChanges(FrakCallTheAdmiralException.class, input);
    }

    // The game and the journal are as before the input, and the same input stays pending.
    private <T extends RuntimeException> T assertNothingChanges(Class<T> thrown, Executable input) {
        val entries = engine.journal().entries();
        val snapshot = snapshot(engine);
        val statuses = statuses(engine);

        val failure = assertThrows(thrown, input);

        assertEquals(entries, engine.journal().entries());
        assertEquals(snapshot, snapshot(engine));
        assertEquals(statuses, statuses(engine));
        return failure;
    }

    // The view is the game itself.
    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }

    private static Set<Followup> options(Followup choice) {
        return ((OneOf) choice).options();
    }
}
