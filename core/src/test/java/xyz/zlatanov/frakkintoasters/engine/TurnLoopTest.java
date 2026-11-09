package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.location.galactica.ResearchLabEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.*;
import static xyz.zlatanov.frakkintoasters.state.GameStep.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;

// A seeded game from its start, played through submit and choose alone. Restore has to give the same game at every pause.
class TurnLoopTest {

    private GameEngine engine;
    // The seats clockwise from the first player, who the seed picks.
    private int        first, second, third;

    @BeforeEach
    void start() {
        engine = GameEngine.start(3, Randomness.seeded(42));
        first = engine.view().turn().currentPlayerNumber();
        second = first % 3 + 1;
        third = second % 3 + 1;
        assertRestoresTheSameGame();
    }

    @Test
    void severalFullTurns() {
        assertWaitingFor(awaiting(first, SelectCharacterEvent.class));
        select(first, WILLIAM_ADAMA);
        select(second, LAURA_ROSLIN);
        select(third, KARA_STARBUCK_THRACE);

        // the titles, the Loyalty Cards and the starting Skill Cards, but none for the first player
        val game = engine.view();
        assertEquals(List.of(LAURA_ROSLIN, WILLIAM_ADAMA, KARA_STARBUCK_THRACE), List.of(game.president(), game.admiral(), game.cag()));
        assertEquals(1, game.presidentHand().size());
        assertEquals(List.of(1, 1, 1), game.players().stream().map(p -> p.loyaltyCards().size()).toList());
        assertWaitingFor(awaiting(second, DrawSkillCardsEvent.class, DRAW_EXACTLY_3));
        draw(second, Map.of(POLITICS, 3));
        draw(third, Map.of(TACTICS, 2, PILOTING, 1));

        // turn 1: Adama moves to the "Research Lab" and draws a card there
        assertTurn(1, first, RECEIVE_SKILLS);
        assertEquals(0, skillCards(first));
        assertWaitingFor(awaiting(first, DrawSkillCardsEvent.class, DRAW_EXACTLY_5));
        draw(first, Map.of(LEADERSHIP, 3, TACTICS, 2));
        assertTurn(1, first, MOVEMENT);
        assertWaitingFor(new AwaitingChoice(first, options(decision(first, MoveEvent.class), new NoOpEvent(first))));
        play(engine -> engine.choose(first, single(decision(first, MoveEvent.class))));
        play(engine -> engine.submit(first, new MoveEvent(first, RESEARCH_LAB, null)));
        assertEquals(Optional.of(RESEARCH_LAB), engine.view().locate(WILLIAM_ADAMA));
        assertTurn(1, first, ACTION);
        assertWaitingFor(new AwaitingChoice(first, options(decision(first, ActionEvent.class), new NoOpEvent(first))));
        play(engine -> engine.choose(first, single(decision(first, ActionEvent.class))));
        play(engine -> engine.submit(first, new ResearchLabEvent(first, ENGINEERING)));
        assertEquals(6, skillCards(first));
        assertEquals(1, crisisCardsDiscarded()); // the Crisis step stub draws one and discards it

        // turns 2 and 3: the others draw and do nothing
        assertTurn(2, second, RECEIVE_SKILLS);
        draw(second, Map.of(POLITICS, 3, LEADERSHIP, 2));
        passMovementAndAction(second);
        assertTurn(3, third, RECEIVE_SKILLS);
        draw(third, Map.of(TACTICS, 2, PILOTING, 2, LEADERSHIP, 1));
        passMovementAndAction(third);
        assertEquals(List.of(6, 8, 8), List.of(skillCards(first), skillCards(second), skillCards(third)));
        assertEquals(3, crisisCardsDiscarded());

        // turn 4: Adama ends his turn with 11 cards, so he discards 1
        assertTurn(4, first, RECEIVE_SKILLS);
        draw(first, Map.of(LEADERSHIP, 3, TACTICS, 2));
        passMovementAndAction(first);
        assertTurn(4, first, DISCARD);
        assertWaitingFor(awaiting(first, DiscardSkillCardsEvent.class, DISCARD_DOWN_TO_HAND_LIMIT));
        val discarded = engine.view().player(first).skillCards().cards().getFirst();
        play(engine -> engine.submit(first, new DiscardSkillCardsEvent(first, List.of(discarded))));
        assertEquals(10, skillCards(first));

        assertTurn(5, second, RECEIVE_SKILLS);
        assertEquals(List.of(2, 2, 1), List.of(turns(first), turns(second), turns(third)));
    }

    // "Stranded - Your character is not placed on the game board at the start of the game. While not on the game board,
    // you may not move, be moved, or take actions. At the start of your 2nd turn, place your character on the "Hangar
    // Deck" location."
    @Test
    void strandedHeloJoinsAtTheStartOfHisSecondTurn() {
        select(first, LAURA_ROSLIN);
        select(second, KARL_HELO_AGATHON);
        select(third, KARA_STARBUCK_THRACE);
        assertEquals(KARL_HELO_AGATHON, engine.view().admiral()); // even off the board
        draw(second, Map.of(LEADERSHIP, 2, TACTICS, 1));
        draw(third, Map.of(TACTICS, 2, PILOTING, 1));

        draw(first, Map.of(POLITICS, 3, LEADERSHIP, 2));
        passMovementAndAction(first);

        // his 1st turn: he draws, then has no Movement and no Action step to take
        assertTurn(2, second, RECEIVE_SKILLS);
        draw(second, Map.of(LEADERSHIP, 2, TACTICS, 2, PILOTING, 1));
        assertTurn(3, third, RECEIVE_SKILLS);
        assertEquals(Optional.empty(), engine.view().locate(KARL_HELO_AGATHON));
        assertEquals(2, crisisCardsDiscarded()); // but he has a Crisis step

        draw(third, Map.of(TACTICS, 2, PILOTING, 2, LEADERSHIP, 1));
        passMovementAndAction(third);
        draw(first, Map.of(POLITICS, 3, LEADERSHIP, 2));
        passMovementAndAction(first);

        // his 2nd turn
        assertTurn(5, second, RECEIVE_SKILLS);
        assertEquals(Optional.of(HANGAR_DECK), engine.view().locate(KARL_HELO_AGATHON));
        draw(second, Map.of(LEADERSHIP, 2, TACTICS, 2, PILOTING, 1));
        assertWaitingFor(new AwaitingChoice(second, options(decision(second, MoveEvent.class), new NoOpEvent(second))));
    }

    /* inputs: each one checks restore at the pause it leads to */

    private void select(int player, Character character) {
        play(engine -> engine.submit(player, new SelectCharacterEvent(player, character)));
    }

    private void draw(int player, Map<SkillCardColor, Integer> selection) {
        play(engine -> engine.submit(player, new DrawSkillCardsEvent(player, selection)));
    }

    private void passMovementAndAction(int player) {
        play(engine -> engine.choose(player, single(new NoOpEvent(player))));
        play(engine -> engine.choose(player, single(new NoOpEvent(player))));
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    /* assertions */

    private void assertTurn(int number, int currentPlayer, GameStep step) {
        val turn = engine.view().turn();
        assertEquals(List.of(number, currentPlayer, step), List.of(turn.number(), turn.currentPlayerNumber(), turn.step()));
    }

    private void assertWaitingFor(PendingInput expected) {
        assertEquals(EngineStatus.waiting(expected), engine.status(expected.player()));
    }

    private void assertRestoresTheSameGame() {
        val restored = GameEngine.restore(engine.journal());

        assertEquals(snapshot(engine), snapshot(restored));
        assertEquals(statuses(engine), statuses(restored));
    }

    /* helpers */

    private static <T extends Event> AwaitingDecision awaiting(int player, Class<T> action, EventConstraint... constraints) {
        return new AwaitingDecision(new PlayerDecisionEvent<>(player, action, constraints));
    }

    private static PlayerDecisionEvent<?> decision(int player, Class<? extends Event> action) {
        return new PlayerDecisionEvent<>(player, action);
    }

    private static LinkedHashSet<Followup> options(Event... events) {
        val options = new LinkedHashSet<Followup>();
        for (val event : events) {
            options.add(single(event));
        }
        return options;
    }

    private int skillCards(int player) {
        return engine.view().player(player).skillCards().size();
    }

    private int turns(int player) {
        return engine.view().player(player).turns();
    }

    private int crisisCardsDiscarded() {
        return ((Game) engine.view()).decks().crisis().discardSize();
    }

    // The view is the game itself.
    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
