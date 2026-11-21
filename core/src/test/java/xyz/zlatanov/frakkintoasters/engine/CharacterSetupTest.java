package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.GameStep.MOVEMENT;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RIOTS_NO_JUMP;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;

// The abilities that change the setup and the Receive Skills step, in a seeded game played through submit and choose.
// Restore has to give the same game at every pause.
class CharacterSetupTest {

    private GameEngine engine;
    // The seats clockwise from the first player, who the seed picks.
    private int        first, second, third;

    @BeforeEach
    void start() {
        engine = new GameEngine(3, Randomness.seeded(42), new StackedStartGameEvent(List.of(RIOTS_NO_JUMP, RIOTS_NO_JUMP)));
        first = engine.view().turn().currentPlayerNumber();
        second = first % 3 + 1;
        third = second % 3 + 1;
    }

    @Test
    void zarekStartsWithAMutinyCardAndAndersOnTheBench() {
        select(first, TOM_ZAREK_ALT);
        // his own "Necessary Steps": he picks that card from the top 2 Mutiny Cards
        val necessarySteps = (AwaitingChoice) engine.pendingInput().orElseThrow();
        assertEquals(first, necessarySteps.player());
        play(engine -> engine.choose(first, necessarySteps.options().iterator().next()));
        select(second, SAMUEL_T_ANDERS);
        select(third, LAURA_ROSLIN);

        // Tom Zarek (alt.)'s "Disreputable": a Mutiny Card from the start
        assertEquals(1, engine.view().player(first).mutinyCards().size());

        // the starting Skill Cards are not the Receive Skills step: Anders gets his
        draw(second, Map.of(LEADERSHIP, 2, TACTICS, 1));
        draw(third, Map.of(POLITICS, 3));

        // turn 1: Zarek draws, passes, and the Admiral (Zarek) chooses the top of "Riots"
        draw(first, Map.of(POLITICS, 2, LEADERSHIP, 2, TACTICS, 1));
        pass(first);
        pass(first);
        play(engine -> engine.choose(first, single(new ChooseCrisisSectionEvent(first, RIOTS_NO_JUMP, TOP))));

        // turn 2: Samuel T. Anders' "Starts on the Bench" skips his first Receive Skills step
        val turn = engine.view().turn();
        assertEquals(List.of(2, second, MOVEMENT), List.of(turn.number(), turn.currentPlayerNumber(), turn.step()));
        assertEquals(3, engine.view().player(second).skillCards().size());
        val pending = engine.pendingInput().orElseThrow();
        assertInstanceOf(AwaitingChoice.class, pending);
        assertEquals(second, pending.player());
    }

    private void select(int player, Character character) {
        play(engine -> engine.submit(player, new SelectCharacterEvent(player, character)));
    }

    private void draw(int player, Map<SkillCardColor, Integer> selection) {
        play(engine -> engine.submit(player, new DrawSkillCardsEvent(player, selection)));
    }

    private void pass(int player) {
        play(engine -> engine.choose(player, single(new NoOpEvent(player))));
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        val restored = GameEngine.restore(engine.journal());
        assertEquals(snapshot(engine), snapshot(restored));
        assertEquals(statuses(engine), statuses(restored));
    }

    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
