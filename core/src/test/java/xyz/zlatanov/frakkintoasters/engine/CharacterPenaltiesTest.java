package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.ConsolidatePowerEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.location.colonialone.PresidentsOfficeEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.turn.StartTurnEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESIDENTS_OFFICE;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;

/**
 * Player 1 is the current player. Each script ends the game after its events.
 */
class CharacterPenaltiesTest {

    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private GameEngine engine;
    private int        players;

    // "Terminal Illness": Laura Roslin discards 2 Skill Cards, then the "President's Office" draws her 2 Politics.
    @Test
    void roslinPaysBeforeActivatingALocation() {
        val first = new SkillCard(1, LAUNCH_SCOUT);
        val second = new SkillCard(2, LAUNCH_SCOUT);
        start(3, all(setUp(LAURA_ROSLIN, SAUL_TIGH, KARA_STARBUCK_THRACE),
                        all(new GiveSkillCardEvent(1, first), new GiveSkillCardEvent(1, second),
                                new MoveCharacterEvent(LAURA_ROSLIN, PRESIDENTS_OFFICE))),
                single(new PlayerDecisionEvent<>(1, ActionEvent.class)));

        play(engine -> engine.submit(1, new PresidentsOfficeEvent(1)));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_EXACTLY_2)));
        assertTrue(engine.view().turn().actionCostPaid(1));
        play(engine -> engine.submit(1, new DiscardSkillCardsEvent(1, List.of(first, second))));

        // the cards she paid with are gone, the 2 Politics the location drew are in her hand
        val hand = engine.view().player(1).skillCards().cards();
        assertEquals(2, hand.size());
        assertTrue(hand.stream().allMatch(card -> card.type().color() == POLITICS));
        assertFalse(engine.view().turn().actionCostPaid(1));
        assertEquals(EngineStatus.finished(Team.CYLONS), engine.status(1));
    }

    // "Reluctant": Louis Hoshi discards another card to play "Consolidate Power"; he can't discard the card he plays.
    @Test
    void hoshiPaysBeforePlayingASkillCardAction() {
        val played = new SkillCard(1, CONSOLIDATE_POWER);
        val other = new SkillCard(1, LAUNCH_SCOUT);
        val third = new SkillCard(2, EXECUTIVE_ORDER);
        start(3, all(setUp(LOUIS_HOSHI, LAURA_ROSLIN, KARA_STARBUCK_THRACE),
                        all(new GiveSkillCardEvent(1, played), new GiveSkillCardEvent(1, other),
                                new GiveSkillCardEvent(1, third))),
                single(new PlayerDecisionEvent<>(1, ActionEvent.class)));

        play(engine -> engine.submit(1, new ConsolidatePowerEvent(1, played)));
        assertWaitingFor(new AwaitingChoice(1, Set.of(
                single(new DiscardSkillCardsEvent(1, List.of(other))),
                single(new DiscardSkillCardsEvent(1, List.of(third))))));
        play(engine -> engine.choose(1, single(new DiscardSkillCardsEvent(1, List.of(other)))));

        // the card is played, and "Consolidate Power" asks for its draw
        assertEquals(List.of(third), engine.view().player(1).skillCards().cards());
        assertTrue(engine.view().decks().skillCardsLeft(POLITICS) > 0);
        assertTrue(engine.pendingInput().orElseThrow() instanceof AwaitingDecision(var decision)
                && decision.action() == DrawSkillCardsEvent.class);
    }

    // "Emotionally Fragile": morale down to 2 executes Anastasia "Dee" Dualla, and her execution costs no morale.
    @Test
    void deeIsExecutedWhenMoraleDropsTo2() {
        start(3, setUp(ANASTASIA_DEE_DUALLA, SAUL_TIGH, KARA_STARBUCK_THRACE), single(new LoseResourceEvent(MORALE, 8)));

        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, SelectCharacterEvent.class)));
        assertEquals(2, engine.view().boards().galactica().morale());
        assertTrue(engine.view().removedComponents().items().contains(ANASTASIA_DEE_DUALLA));
        assertFalse(engine.view().moraleLostToExecution());
    }

    // "Alcoholic": at the start of a turn, Saul Tigh discards his only Skill Card, whoever's turn it is.
    @Test
    void saulTighDiscardsHisOnlyCardAtTheStartOfATurn() {
        start(3, all(setUp(LAURA_ROSLIN, SAUL_TIGH, KARA_STARBUCK_THRACE),
                single(new GiveSkillCardEvent(2, new SkillCard(1, LAUNCH_SCOUT)))), single(new StartTurnEvent()));

        assertTrue(engine.view().player(2).skillCards().isEmpty());
    }

    // The characters by seat, then the titles.
    private static Followup setUp(Character... characters) {
        val events = new ArrayList<Event>();
        for (int i = 0; i < characters.length; i++) {
            events.add(new SelectCharacterEvent(i + 1, characters[i]));
        }
        events.add(new DetermineNextPresidentEvent());
        events.add(new DetermineNextAdmiralEvent());
        events.add(new DetermineNextCagEvent());
        return all(events.toArray(Event[]::new));
    }

    private void start(int playerCount, Followup setUp, Followup events) {
        players = playerCount;
        engine = new GameEngine(playerCount, Randomness.seeded(42), new ScriptEvent(all(setUp, events, CYLONS_WIN)));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
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

    private List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, players).mapToObj(engine::status).toList();
    }
}
