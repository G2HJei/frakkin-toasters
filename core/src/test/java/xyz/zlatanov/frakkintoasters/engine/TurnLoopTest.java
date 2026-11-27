package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.character.ReligiousVisionsEvent;
import xyz.zlatanov.frakkintoasters.event.character.SecretDestinyEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ResolveCrisisCardEvent;
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
import xyz.zlatanov.frakkintoasters.event.action.MovementAbilityEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardPlays;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.ResearchLabEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RIOTS_NO_JUMP;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.*;
import static xyz.zlatanov.frakkintoasters.state.GameStep.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;

class TurnLoopTest {

    // Simple Crisis Cards, one a turn: nothing to ask but the chooser's section, and no die rolls or interrupt windows. "Riots"
    // costs 1 food and 1 morale on top, and "Detente" advances the Pursuit track, so the CAG places a civilian ship.
    // Laura Roslin draws 2 each turn ("Religious Visions"), so more are stacked than turns are played.
    private static final List<CrisisCard> CRISIS_CARDS = List.of(RIOTS_NO_JUMP, DETENTE, RIOTS_NO_JUMP, RIOTS_NO_JUMP,
            RIOTS_NO_JUMP, RIOTS_NO_JUMP);

    private GameEngine engine;
    // The seats clockwise from the first player, who the seed picks.
    private int        first, second, third;

    @BeforeEach
    void start() {
        engine = new GameEngine(3, Randomness.seeded(42), new StackedStartGameEvent(CRISIS_CARDS));
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

        // the Admiral chooses the top of "Riots": -1 food and -1 morale
        assertTurn(1, first, CRISIS);
        resolveCrisis(RIOTS_NO_JUMP);
        assertEquals(List.of(7, 9), List.of(food(), morale()));
        assertEquals(1, crisisCardsDiscarded()); // at the end of the turn (rule 8.8)

        // turns 2 and 3: the others draw and do nothing, and the CAG places the civilian ship that "Detente" brings
        assertTurn(2, second, RECEIVE_SKILLS);
        draw(second, Map.of(POLITICS, 3, LEADERSHIP, 2));
        passMovementAndAction(second);
        religiousVisions(second, DETENTE, RIOTS_NO_JUMP);
        resolveCrisis(DETENTE);
        placeACivilianShip();
        assertTurn(3, third, RECEIVE_SKILLS);
        draw(third, Map.of(TACTICS, 2, PILOTING, 2, LEADERSHIP, 1));
        passMovementAndAction(third);
        resolveCrisis(RIOTS_NO_JUMP);
        assertEquals(List.of(6, 8, 8), List.of(skillCards(first), skillCards(second), skillCards(third)));
        assertEquals(3, crisisCardsDiscarded());
        assertEquals(List.of(6, 8), List.of(food(), morale()));

        // turn 4: Adama ends his turn with 11 cards, so he discards 1
        assertTurn(4, first, RECEIVE_SKILLS);
        draw(first, Map.of(LEADERSHIP, 3, TACTICS, 2));
        passMovementAndAction(first);
        resolveCrisis(RIOTS_NO_JUMP);
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
        religiousVisions(first, RIOTS_NO_JUMP, DETENTE);
        resolveCrisis(RIOTS_NO_JUMP); // Helo is the Admiral, even off the board

        // his 1st turn: he draws, then has no Movement and no Action step to take
        assertTurn(2, second, RECEIVE_SKILLS);
        draw(second, Map.of(LEADERSHIP, 2, TACTICS, 2, PILOTING, 1));
        resolveCrisis(RIOTS_NO_JUMP); // but he has a Crisis step
        assertTurn(3, third, RECEIVE_SKILLS);
        assertEquals(Optional.empty(), engine.view().locate(KARL_HELO_AGATHON));
        assertEquals(2, crisisCardsDiscarded());

        draw(third, Map.of(TACTICS, 2, PILOTING, 2, LEADERSHIP, 1));
        passMovementAndAction(third);
        resolveCrisis(RIOTS_NO_JUMP);
        draw(first, Map.of(POLITICS, 3, LEADERSHIP, 2));
        passMovementAndAction(first);
        resolveCrisis(RIOTS_NO_JUMP); // she drew 2 "Riots", so there was nothing to choose

        // his 2nd turn
        assertTurn(5, second, RECEIVE_SKILLS);
        assertEquals(Optional.of(HANGAR_DECK), engine.view().locate(KARL_HELO_AGATHON));
        draw(second, Map.of(LEADERSHIP, 2, TACTICS, 2, PILOTING, 1));
        // he has his Movement step, with a "Movement:" card when the seed dealt him one
        val movementCard = SkillCardPlays.canPlayMovementAbility(engine.view(), engine.view().player(second));
        assertWaitingFor(new AwaitingChoice(second, movementCard
                ? options(decision(second, MoveEvent.class), decision(second, MovementAbilityEvent.class), new NoOpEvent(second))
                : options(decision(second, MoveEvent.class), new NoOpEvent(second))));
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

    // "Religious Visions": Laura Roslin draws 2 and keeps the first.
    private void religiousVisions(int roslin, CrisisCard kept, CrisisCard putAtBottom) {
        assertWaitingFor(new AwaitingChoice(roslin, options(new ReligiousVisionsEvent(roslin, kept, putAtBottom),
                new ReligiousVisionsEvent(roslin, putAtBottom, kept))));
        play(engine -> engine.choose(roslin, single(new ReligiousVisionsEvent(roslin, kept, putAtBottom))));
    }

    // Kara "Starbuck" Thrace lets the revealed card resolve ("Secret Destiny"), then the title holder that the card names
    // chooses its top section.
    private void resolveCrisis(CrisisCard card) {
        val starbuck = engine.view().playerNumber(KARA_STARBUCK_THRACE);
        assertWaitingFor(new AwaitingChoice(starbuck, options(new SecretDestinyEvent(starbuck, card),
                new ResolveCrisisCardEvent(card))));
        play(engine -> engine.choose(starbuck, single(new ResolveCrisisCardEvent(card))));
        val game = engine.view();
        val holder = switch (card.chooser()) {
            case ADMIRAL -> game.admiral();
            case CAG -> game.cag();
            case PRESIDENT -> game.president();
            case CURRENT_PLAYER -> game.currentPlayer().character();
        };
        val chooser = game.playerNumber(holder);
        assertWaitingFor(new AwaitingChoice(chooser, options(new ChooseCrisisSectionEvent(chooser, card, TOP),
                new ChooseCrisisSectionEvent(chooser, card, BOTTOM))));
        play(engine -> engine.choose(chooser, single(new ChooseCrisisSectionEvent(chooser, card, TOP))));
    }

    // "Detente" moves the Pursuit marker onto a space with a civilian ship: the CAG places it where there is none yet.
    private void placeACivilianShip() {
        val cag = engine.view().playerNumber(engine.view().cag());
        assertWaitingFor(awaiting(cag, PlaceCivilianShipEvent.class));
        play(engine -> engine.submit(cag, new PlaceCivilianShipEvent(cag, GALACTICA_SPACE_12_OCLOCK)));
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

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

    private int food() {
        return engine.view().boards().galactica().food();
    }

    private int morale() {
        return engine.view().boards().galactica().morale();
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
