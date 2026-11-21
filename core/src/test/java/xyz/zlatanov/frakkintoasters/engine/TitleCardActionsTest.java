package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.ViperActivation;
import xyz.zlatanov.frakkintoasters.event.action.title.CagPilotActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.title.LaunchNukeEvent;
import xyz.zlatanov.frakkintoasters.event.action.title.PassCagTitleEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.HangarDeckEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeCagEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.PlaceShipEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActionStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.TurnLimit.CAG_PILOT_ACTION;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;

/**
 * The Admiral's and the CAG's Title Card actions through {@code submit} and {@code choose} alone, restoring the game at
 * every pause.
 */
class TitleCardActionsTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private GameEngine engine;

    @Test
    void theAdmiralLaunchesANukeAtASpaceArea() {
        start(all(setUp(WILLIAM_ADAMA), single(new MakeAdmiralEvent(WILLIAM_ADAMA)),
                single(new PlaceShipEvent(BASESTAR, GALACTICA_SPACE_4_OCLOCK)), single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new LaunchNukeEvent(1, GALACTICA_SPACE_4_OCLOCK)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(1, engine.view().nukes());
        // the seed decides the roll: from 3 the basestar is destroyed, below it is damaged twice
        val basestars = engine.view().boards().galactica().shipsIn(GALACTICA_SPACE_4_OCLOCK, Basestar.class);
        if (engine.view().turn().dieRoll().result() >= 3) {
            assertEquals(List.of(), basestars);
        } else {
            assertTrue(basestars.isEmpty() || basestars.getFirst().damage().size() == 2);
        }
    }

    @Test
    void theCagActivatesAViperAndPassesTheTitle() {
        start(all(setUp(KARA_STARBUCK_THRACE), single(new MakeCagEvent(KARA_STARBUCK_THRACE)),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1,
                new PassCagTitleEvent(1, ViperActivation.launch(VIPER, GALACTICA_SPACE_4_OCLOCK), 2)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(WILLIAM_ADAMA, engine.view().cag());
        assertEquals(1, unmannedFightersInSpace().size());
    }

    // The Hangar Deck launches the CAG, who then activates an unmanned viper and takes another action, once a turn.
    @Test
    void thePilotingCagActivatesAViperAndActsAgainOnceATurn() {
        start(all(setUp(KARA_STARBUCK_THRACE), single(new MakeCagEvent(KARA_STARBUCK_THRACE)),
                single(new MoveCharacterEvent(KARA_STARBUCK_THRACE, HANGAR_DECK)), single(new ActionStepEvent()),
                CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new HangarDeckEvent(1)));
        play(engine -> engine.submit(1, new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_4_OCLOCK, KARA_STARBUCK_THRACE, null)));
        act(1);
        play(engine -> engine.submit(1, new CagPilotActionEvent(1, ViperActivation.launch(VIPER, GALACTICA_SPACE_6_OCLOCK))));

        assertTrue(engine.view().turn().hasUsed(1, CAG_PILOT_ACTION));
        assertEquals(new AwaitingChoice(1, Set.of(
                single(new PlayerDecisionEvent<>(1, ActivateViperEvent.class)),
                single(new PlayerDecisionEvent<>(1, ActionEvent.class)),
                single(new NoOpEvent(1)))), engine.pendingInput().orElseThrow());
        act(1);
        val again = new CagPilotActionEvent(1, ViperActivation.launch(VIPER, GALACTICA_SPACE_4_OCLOCK));
        assertThrows(InvalidActionException.class, () -> engine.submit(1, again));
        val unmanned = unmannedFightersInSpace().getFirst().id();
        play(engine -> engine.submit(1,
                new PassCagTitleEvent(1, ViperActivation.move(unmanned, GALACTICA_SPACE_8_OCLOCK), 3)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(LAURA_ROSLIN, engine.view().cag());
        assertTrue(engine.view().boards().galactica().pilotedFighter(KARA_STARBUCK_THRACE).isPresent());
    }

    private static Followup setUp(Character first) {
        return all(
                new SelectCharacterEvent(1, first),
                new SelectCharacterEvent(2, first == WILLIAM_ADAMA ? KARA_STARBUCK_THRACE : WILLIAM_ADAMA),
                new SelectCharacterEvent(3, LAURA_ROSLIN));
    }

    private List<HumanFighterView> unmannedFightersInSpace() {
        return engine.view().boards().galactica().shipsInSpace(HumanFighterView.class).stream()
                .filter(fighter -> fighter.pilot() == null)
                .toList();
    }

    private void start(Followup script) {
        engine = new GameEngine(3, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    // The player chooses to take an action.
    private void act(int player) {
        val option = single(new PlayerDecisionEvent<>(player, ActionEvent.class));
        val choice = (AwaitingChoice) engine.pendingInput().orElseThrow();
        assertTrue(choice.options().contains(option), choice.options().toString());
        play(engine -> engine.choose(player, option));
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
