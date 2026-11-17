package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.ResearchLabEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_12_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RIOTS_NO_JUMP;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;

/**
 * A seeded 3 player game from its start through four turns, as the inputs its players give: the characters, the
 * starting Skill Cards, a move and an action, the Crisis Cards' sections, a civilian ship and a discard. The Crisis
 * Cards are stacked, so nothing but the section is asked. The inputs depend on the first player the seed picks, so each
 * one is computed from the game as it stands.
 */
public final class ScriptedTurns {

    public static final List<CrisisCard> CRISIS_CARDS = List.of(RIOTS_NO_JUMP, DETENTE, RIOTS_NO_JUMP, RIOTS_NO_JUMP);
    public static final long             SEED         = 42;

    /**
     * An answer to the pending input: an event to submit, or an option to choose.
     */
    public record Input(int player, Event submit, Followup choose) {

        public void applyTo(GameEngine engine) {
            if (submit != null) {
                engine.submit(player, submit);
            } else {
                engine.choose(player, choose);
            }
        }
    }

    private ScriptedTurns() {
    }

    public static GameEngine start() {
        return TestEngines.start(3, SEED, firstEvent());
    }

    public static Event firstEvent() {
        return new StackedStartGameEvent(CRISIS_CARDS);
    }

    /**
     * @return the inputs in order. Each is computed from the game when it is its turn
     */
    public static List<Function<GameView, Input>> inputs() {
        val inputs = new ArrayList<Function<GameView, Input>>();
        // the seats clockwise from the first player
        Function<GameView, Integer> first = game -> firstPlayer(game);
        Function<GameView, Integer> second = game -> firstPlayer(game) % 3 + 1;
        Function<GameView, Integer> third = game -> (firstPlayer(game) % 3 + 1) % 3 + 1;

        inputs.add(game -> submit(first.apply(game), p -> new SelectCharacterEvent(p, WILLIAM_ADAMA)));
        inputs.add(game -> submit(second.apply(game), p -> new SelectCharacterEvent(p, LAURA_ROSLIN)));
        inputs.add(game -> submit(third.apply(game), p -> new SelectCharacterEvent(p, KARA_STARBUCK_THRACE)));
        inputs.add(game -> draw(second.apply(game), Map.of(POLITICS, 3)));
        inputs.add(game -> draw(third.apply(game), Map.of(TACTICS, 2, PILOTING, 1)));

        // turn 1: Adama moves to the "Research Lab" and draws a card there
        inputs.add(game -> draw(first.apply(game), Map.of(LEADERSHIP, 3, TACTICS, 2)));
        inputs.add(game -> chooseDecision(first.apply(game), MoveEvent.class));
        inputs.add(game -> submit(first.apply(game), p -> new MoveEvent(p, RESEARCH_LAB, null)));
        inputs.add(game -> chooseDecision(first.apply(game), ActionEvent.class));
        inputs.add(game -> submit(first.apply(game), p -> new ResearchLabEvent(p, ENGINEERING)));
        inputs.add(game -> crisisTop(game, RIOTS_NO_JUMP));

        // turns 2 and 3: the others draw and pass, and the CAG places the civilian ship that "Detente" brings
        inputs.add(game -> draw(second.apply(game), Map.of(POLITICS, 3, LEADERSHIP, 2)));
        inputs.add(game -> pass(second.apply(game)));
        inputs.add(game -> pass(second.apply(game)));
        inputs.add(game -> crisisTop(game, DETENTE));
        inputs.add(game -> submit(game.playerNumber(game.cag()), p -> new PlaceCivilianShipEvent(p, GALACTICA_SPACE_12_OCLOCK)));
        inputs.add(game -> draw(third.apply(game), Map.of(TACTICS, 2, PILOTING, 2, LEADERSHIP, 1)));
        inputs.add(game -> pass(third.apply(game)));
        inputs.add(game -> pass(third.apply(game)));
        inputs.add(game -> crisisTop(game, RIOTS_NO_JUMP));

        // turn 4: Adama ends his turn with 11 cards, so he discards 1
        inputs.add(game -> draw(first.apply(game), Map.of(LEADERSHIP, 3, TACTICS, 2)));
        inputs.add(game -> pass(first.apply(game)));
        inputs.add(game -> pass(first.apply(game)));
        inputs.add(game -> crisisTop(game, RIOTS_NO_JUMP));
        inputs.add(game -> {
            val player = first.apply(game);
            val discarded = game.player(player).skillCards().cards().getFirst();
            return new Input(player, new DiscardSkillCardsEvent(player, List.of(discarded)), null);
        });
        return inputs;
    }

    /**
     * Plays every input. The game then waits for the second player's Receive Skills step of turn 5.
     */
    public static GameEngine play() {
        val engine = start();
        inputs().forEach(input -> input.apply(engine.view()).applyTo(engine));
        return engine;
    }

    // The first player is the current player of turn 1, and before it the first to select a character.
    private static int firstPlayer(GameView game) {
        val turn = game.turn();
        if (turn.number() == 0) {
            return turn.currentPlayerNumber();
        }
        return Math.floorMod(turn.currentPlayerNumber() - 1 - (turn.number() - 1), 3) + 1;
    }

    private static Input submit(int player, Function<Integer, Event> event) {
        return new Input(player, event.apply(player), null);
    }

    private static Input draw(int player, Map<SkillCardColor, Integer> selection) {
        return new Input(player, new DrawSkillCardsEvent(player, selection), null);
    }

    private static Input pass(int player) {
        return new Input(player, null, single(new NoOpEvent(player)));
    }

    private static Input chooseDecision(int player, Class<? extends Event> action) {
        return new Input(player, null, single(new PlayerDecisionEvent<>(player, action)));
    }

    // The title holder that the card names chooses its top section.
    private static Input crisisTop(GameView game, CrisisCard card) {
        val holder = switch (card.chooser()) {
            case ADMIRAL -> game.admiral();
            case CAG -> game.cag();
            case PRESIDENT -> game.president();
            case CURRENT_PLAYER -> game.currentPlayer().character();
        };
        val chooser = game.playerNumber(holder);
        return new Input(chooser, null, single(new ChooseCrisisSectionEvent(chooser, card, TOP)));
    }
}
