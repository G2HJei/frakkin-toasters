package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceInSetupLocationEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActivateCylonShipsStepEvent;
import xyz.zlatanov.frakkintoasters.event.turn.CrisisStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_EXACTLY_1;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTIVATE_CYLON_SHIPS;
import static xyz.zlatanov.frakkintoasters.state.GameStep.CRISIS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PEGASUS_CIC;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.*;
import static xyz.zlatanov.frakkintoasters.state.track.Pursuit.POSITION_2;

/**
 * The Crisis step from the card to the end of the Activate Cylon Ships step, through {@code submit} and {@code choose}
 * alone, restoring the game at every pause. Nobody holds a card and there are no ships on the main game board, so that
 * no interrupt window and no attack asks anything.
 */
class CrisisStepTest {

    // Only the end of the game empties the agenda, so the script ends it after the steps.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    // Helena Cain is the Admiral, Laura Roslin the President and Kara Thrace the CAG. Player 1 is the current player.
    private static Followup setUp(CrisisCard... topToBottom) {
        val stacking = new java.util.ArrayList<Followup>();
        for (val card : List.of(topToBottom).reversed()) {
            stacking.add(single(new StackCrisisCardEvent(card)));
        }
        return all(
                all(new SelectCharacterEvent(1, HELENA_CAIN),
                        new SelectCharacterEvent(2, LAURA_ROSLIN),
                        new SelectCharacterEvent(3, KARA_STARBUCK_THRACE),
                        new DetermineNextPresidentEvent(),
                        new DetermineNextAdmiralEvent(),
                        new DetermineNextCagEvent()),
                all(stacking.toArray(Followup[]::new)));
    }

    private GameEngine engine;

    // "Skill Check Crises ... are resolved according to the rules for skill checks", and the current player may choose the
    // alternate instruction instead (rule 8.5.1.1).
    @Test
    void theCurrentPlayerChoosesTheSkillCheck() {
        start(all(setUp(WATER_SABOTAGED), single(new CrisisStepEvent()), CYLONS_WIN));

        assertWaitingFor(sectionChoice(1, WATER_SABOTAGED));
        play(engine -> engine.choose(1, section(1, WATER_SABOTAGED, TOP)));

        assertEquals(SkillCheckType.WATER_SABOTAGED, engine.view().activeSkillCheck().type());
        // from the player to the left of the current player (rule 10.3.1)
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(2, PlayCardsBeforeSkillCheckEvent.class)));
        assertEquals(CRISIS, engine.view().turn().step());
        assertEquals(8, engine.view().boards().galactica().food());
    }

    @Test
    void theCurrentPlayerChoosesTheAlternateInstruction() {
        start(all(setUp(WATER_SABOTAGED), single(new CrisisStepEvent()), CYLONS_WIN));

        play(engine -> engine.choose(1, section(1, WATER_SABOTAGED, BOTTOM)));

        assertNull(engine.view().activeSkillCheck());
        assertEquals(7, engine.view().boards().galactica().food());
        assertEquals(new EngineStatus.Finished(xyz.zlatanov.frakkintoasters.state.Team.CYLONS), engine.status(1));
    }

    @Test
    void fulfillerOfProphecyReturnsToTheCrisisStep() {
        start(all(setUp(FULFILLER_OF_PROPHECY, RIOTS_NO_JUMP),
                single(new CrisisStepEvent()), single(new ActivateCylonShipsStepEvent()), CYLONS_WIN));

        play(engine -> engine.choose(1, section(1, FULFILLER_OF_PROPHECY, BOTTOM)));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_EXACTLY_1)));
        play(engine -> engine.submit(1, new DiscardSkillCardsEvent(1, List.of()))); // nothing to discard

        // The first card activates its ships ("Activate Cylon Basestars": none on the board, so one goes to the Cylon Fleet
        // board and the Pursuit marker moves onto a civilian ship), then the new card is drawn.
        assertEquals(ACTIVATE_CYLON_SHIPS, engine.view().turn().step());
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(3, PlaceCivilianShipEvent.class)));
        play(engine -> engine.submit(3, new PlaceCivilianShipEvent(3, xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_12_OCLOCK)));

        assertEquals(CRISIS, engine.view().turn().step());
        assertEquals(List.of(FULFILLER_OF_PROPHECY, RIOTS_NO_JUMP), engine.view().turn().crisisCards());
        assertWaitingFor(sectionChoice(1, RIOTS_NO_JUMP)); // the Admiral
        play(engine -> engine.choose(1, section(1, RIOTS_NO_JUMP, TOP)));

        // "Riots" costs 1 food and 1 morale, and its ships activate in the Activate Cylon Ships step of its own, which
        // advances the Pursuit track again.
        assertEquals(List.of(7, 9), List.of(engine.view().boards().galactica().food(), engine.view().boards().galactica().morale()));
        assertEquals(POSITION_2, engine.view().boards().cylonFleet().pursuitTrack());
        assertEquals(ACTIVATE_CYLON_SHIPS, engine.view().turn().step());
        assertEquals(false, engine.view().turn().returnsToCrisisStep());
        assertEquals(new EngineStatus.Finished(xyz.zlatanov.frakkintoasters.state.Team.CYLONS), engine.status(1));
    }

    /* inputs */

    private static AwaitingChoice sectionChoice(int chooser, CrisisCard card) {
        return new AwaitingChoice(chooser, Set.of(section(chooser, card, TOP), section(chooser, card, BOTTOM)));
    }

    private static Followup section(int chooser, CrisisCard card, Section section) {
        return single(new ChooseCrisisSectionEvent(chooser, card, section));
    }

    private void start(Followup script) {
        engine = new GameEngine(3, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
        // Helena Cain chooses where she starts.
        play(engine -> engine.choose(1, single(new PlaceInSetupLocationEvent(1, PEGASUS_CIC))));
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    /* assertions */

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

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
