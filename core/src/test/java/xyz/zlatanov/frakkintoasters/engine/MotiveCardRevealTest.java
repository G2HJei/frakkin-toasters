package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealMotiveCardEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.DamageLocationEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActionStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.FTL_CONTROL;
import static xyz.zlatanov.frakkintoasters.state.card.MotiveCard.END_THE_CHASE;
import static xyz.zlatanov.frakkintoasters.state.card.MotiveCard.MAKE_AN_ALLY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.MOTIVE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;

/**
 * A Cylon Leader reveals a Motive Card while the game waits for something else (rule 6.2.2.1). Cavil is player 1 and the
 * current player, with "End the Chase" and "Make an Ally"; Kara "Starbuck" Thrace is player 2, William Adama (Admiral)
 * player 3, Laura Roslin (President) player 4.
 */
class MotiveCardRevealTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private static final PlayerDecisionEvent<DrawSkillCardsEvent> STARBUCK_DRAWS =
            new PlayerDecisionEvent<>(2, DrawSkillCardsEvent.class, DRAW_EXACTLY_2);

    private GameEngine engine;

    // "End the Chase" needs "FTL Control" or the "Admiral's Quarters" damaged. He reveals it while another player decides.
    @Test
    void aCylonLeaderRevealsAMotiveCardWhileAnotherPlayerDecides() {
        start(all(setUp(), single(new DamageLocationEvent(FTL_CONTROL)), single(STARBUCK_DRAWS), CYLONS_WIN));

        play(engine -> engine.submit(1, new RevealMotiveCardEvent(1, END_THE_CHASE)));

        assertEquals(List.of(END_THE_CHASE), game().player(1).motiveCards().revealedCards());
        assertWaitingFor(new AwaitingDecision(STARBUCK_DRAWS));
        play(engine -> engine.submit(2, new DrawSkillCardsEvent(2, Map.of(TACTICS, 2))));
        assertEquals(EngineStatus.finished(CYLONS), engine.status(1));
    }

    // He is the one choosing, and the choice stays pending after the reveal.
    @Test
    void aCylonLeaderRevealsAMotiveCardWhileHeChooses() {
        start(all(setUp(), single(new DamageLocationEvent(FTL_CONTROL)), single(new ActionStepEvent()), CYLONS_WIN));
        val choice = (AwaitingChoice) engine.pendingInput().orElseThrow();

        play(engine -> engine.submit(1, new RevealMotiveCardEvent(1, END_THE_CHASE)));

        assertEquals(choice, engine.pendingInput().orElseThrow());
        assertTrue(choice.options().contains(single(new NoOpEvent(1))), choice.options().toString());
        play(engine -> engine.choose(1, single(new NoOpEvent(1))));
        assertEquals(List.of(END_THE_CHASE), game().player(1).motiveCards().revealedCards());
        assertEquals(EngineStatus.finished(CYLONS), engine.status(1));
    }

    // Nothing is damaged, so "End the Chase" can't be revealed, and the game is as before.
    @Test
    void aCardWhoseRequirementIsntMetIsRejected() {
        start(all(setUp(), single(STARBUCK_DRAWS), CYLONS_WIN));
        val journal = engine.journal().entries();

        assertThrows(InvalidActionException.class, () -> engine.submit(1, new RevealMotiveCardEvent(1, END_THE_CHASE)));

        assertEquals(journal, engine.journal().entries());
        assertTrue(game().player(1).motiveCards().revealedCards().isEmpty());
    }

    @Test
    void undoTakesTheRevealBack() {
        start(all(setUp(), single(new DamageLocationEvent(FTL_CONTROL)), single(STARBUCK_DRAWS), CYLONS_WIN));
        play(engine -> engine.submit(1, new RevealMotiveCardEvent(1, END_THE_CHASE)));

        engine.undo();

        assertTrue(game().player(1).motiveCards().revealedCards().isEmpty());
        assertWaitingFor(new AwaitingDecision(STARBUCK_DRAWS));
    }

    private static Followup setUp() {
        return all(
                new SelectCharacterEvent(1, CAVIL),
                new SelectCharacterEvent(2, KARA_STARBUCK_THRACE),
                new SelectCharacterEvent(3, WILLIAM_ADAMA),
                new SelectCharacterEvent(4, LAURA_ROSLIN),
                new MakePresidentEvent(LAURA_ROSLIN),
                new MakeAdmiralEvent(WILLIAM_ADAMA),
                new StackDeckEvent(MOTIVE, List.of(END_THE_CHASE, MAKE_AN_ALLY)),
                new DrawMotiveCardsEvent(1));
    }

    private void start(Followup script) {
        engine = new GameEngine(4, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    private Game game() {
        return (Game) engine.view();
    }

    private void assertWaitingFor(PendingInput expected) {
        assertEquals(EngineStatus.waiting(expected), engine.status(expected.player()));
    }

    private void assertRestoresTheSameGame() {
        val restored = GameEngine.restore(engine.journal());

        assertEquals(snapshot(engine), snapshot(restored));
        assertEquals(statuses(engine), statuses(restored));
    }

    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 4).mapToObj(engine::status).toList();
    }
}
