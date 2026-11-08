package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ChoiceMade;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.FactRecorded;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeClosed;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.ResearchLabEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.LeaveBrigEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceInSetupLocationEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.zlatanov.frakkintoasters.engine.journal.Journal.ROOT_SCOPE;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_5;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
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

    private static final Followup CAIN_SETUP   = one(
            new PlaceInSetupLocationEvent(1, PEGASUS_CIC),
            new PlaceInSetupLocationEvent(1, COMMAND));
    private static final Followup FUEL_OR_FOOD = one(new LoseResourceEvent(FUEL, 1), new LoseResourceEvent(FOOD, 1));

    // Helena Cain has two setup locations, so selecting her queues a choice, which runs before player 2's decision.
    private static final Followup SCRIPT = all(
            single(PLAYER_1_SELECTS),
            single(PLAYER_2_SELECTS),
            single(PLAYER_2_DRAWS_2),
            FUEL_OR_FOOD,
            single(new NoOpEvent(3)));

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

        assertEquals(new Finished(), engine.status(1));
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
                        new ScopeClosed(7, NONE)),
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
        engine = start(all(single(PLAYER_1_SELECTS), single(new PlayerDecisionEvent<>(1, ActionEvent.class))));
        engine.submit(1, new SelectCharacterEvent(1, SAUL_TIGH));

        assertRejected(() -> engine.submit(1, new NoOpEvent(1)));
        engine.submit(1, new ResearchLabEvent(1, ENGINEERING));

        assertEquals(1, engine.view().player(1).skillCards().size());
        assertEquals(new Finished(), engine.status(1));
    }

    @Test
    void aFinishedGameTakesNoInput() {
        engine = start(single(new NoOpEvent(1)));

        assertEquals(new Finished(), engine.status(1));
        assertThrows(InvalidActionException.class, () -> engine.submit(1, new NoOpEvent(1)));
        assertThrows(InvalidActionException.class, () -> engine.choose(1, single(new NoOpEvent(1))));
    }

    /* bugs */

    @Test
    void aQueuedEventThatFailsValidationStopsTheGame() {
        engine = start(all(single(PLAYER_1_SELECTS), single(new LeaveBrigEvent(1, COMMAND))));

        assertThrows(FrakCallTheAdmiralException.class, () -> engine.submit(1, new SelectCharacterEvent(1, SAUL_TIGH)));
        assertStopped();
    }

    @Test
    void aQueuedEventWithoutAProcessorStopsTheGame() {
        engine = start(one(new NoOpEvent(1), new CylonsWinEvent()));

        assertThrows(FrakCallTheAdmiralException.class, () -> engine.choose(1, single(new CylonsWinEvent())));
        assertStopped();
    }

    // QuorumChamberEvent offers this until Quorum Cards exist.
    @Test
    void aDecisionNobodyCanAnswerStopsTheGame() {
        val playQuorumCard = single(new PlayerDecisionEvent<>(1, PlayQuorumCardEvent.class));
        engine = start(one(single(new NoOpEvent(1)), playQuorumCard));

        assertThrows(FrakCallTheAdmiralException.class, () -> engine.choose(1, playQuorumCard));
        assertStopped();
    }

    @Test
    void aDecisionCanHaveConstraintsOnlyOnAConstrainedEvent() {
        assertThrows(FrakCallTheAdmiralException.class,
                () -> start(single(new PlayerDecisionEvent<>(1, SelectCharacterEvent.class, DRAW_EXACTLY_2))));
    }

    @Test
    void theOptionsOfAChoiceCantNameDifferentPlayers() {
        assertThrows(FrakCallTheAdmiralException.class, () -> start(one(new NoOpEvent(1), new NoOpEvent(2))));
    }

    private static GameEngine start(Followup script) {
        return new GameEngine(3, Randomness.seeded(42), new ScriptEvent(script));
    }

    private void playScript() {
        engine.submit(1, new SelectCharacterEvent(1, HELENA_CAIN));
        engine.choose(1, single(new PlaceInSetupLocationEvent(1, COMMAND)));
        engine.submit(2, new SelectCharacterEvent(2, LAURA_ROSLIN));
        engine.submit(2, new DrawSkillCardsEvent(2, Map.of(POLITICS, 2)));
        engine.choose(1, single(new LoseResourceEvent(FUEL, 1)));
    }

    private void assertWaitingFor(PendingInput expected) {
        assertEquals(EngineStatus.waiting(expected), engine.status(1));
    }

    // Nothing changes, and the same input stays pending.
    private void assertRejected(Executable input) {
        List<JournalEntry> entries = engine.journal().entries();
        val status = engine.status(1);

        assertThrows(InvalidActionException.class, input);

        assertEquals(entries, engine.journal().entries());
        assertEquals(status, engine.status(1));
    }

    private void assertStopped() {
        assertThrows(FrakCallTheAdmiralException.class, () -> engine.status(1));
        assertThrows(FrakCallTheAdmiralException.class, () -> engine.submit(1, new NoOpEvent(1)));
        assertThrows(FrakCallTheAdmiralException.class, () -> engine.choose(1, single(new NoOpEvent(1))));
    }

    private static Set<Followup> options(Followup choice) {
        return ((OneOf) choice).options();
    }
}
