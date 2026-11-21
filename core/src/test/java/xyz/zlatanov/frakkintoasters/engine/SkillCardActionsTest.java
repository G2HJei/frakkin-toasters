package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.BuildNukeEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.ConsolidatePowerEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.HangarDeckEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActionStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.PILOTING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

/**
 * Skill Cards played for their "Action:" or "Movement:" through {@code submit} and {@code choose} alone, restoring the
 * game at every pause. The cards are dealt with {@link GiveSkillCardEvent}, which a seeded game can't choose.
 */
class SkillCardActionsTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private static final SkillCard CONSOLIDATE_POWER_1 = new SkillCard(1, CONSOLIDATE_POWER);
    private static final SkillCard BUILD_NUKE_6        = new SkillCard(6, BUILD_NUKE);

    private GameEngine engine;

    @Test
    void consolidatePowerDrawsTwoCardsOfAnyColor() {
        start(all(setUp(WILLIAM_ADAMA), single(new GiveSkillCardEvent(1, CONSOLIDATE_POWER_1)), single(new ActionStepEvent()),
                CYLONS_WIN));

        act(1);
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, ActionEvent.class)));
        play(engine -> engine.submit(1, new ConsolidatePowerEvent(1, CONSOLIDATE_POWER_1)));

        // outside William Adama's skill set
        play(engine -> engine.submit(1, new DrawSkillCardsEvent(1, Map.of(PILOTING, 1, TREACHERY, 1))));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(Set.of(PILOTING, TREACHERY), colors(engine.view().player(1).skillCards().cards()));
        assertEquals(List.of(CONSOLIDATE_POWER), engine.view().turn().playedSkillCards());
    }

    // A character in "Sickbay" has no location action, but may play a card.
    @Test
    void buildNukeFromSickbay() {
        start(all(setUp(WILLIAM_ADAMA), all(new GiveSkillCardEvent(1, BUILD_NUKE_6), new SendToSickbayEvent(1)),
                single(new ActionStepEvent()), CYLONS_WIN));

        assertEquals(SICKBAY, engine.view().locate(WILLIAM_ADAMA).orElseThrow());
        act(1);
        play(engine -> engine.submit(1, new BuildNukeEvent(1, BUILD_NUKE_6)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(3, engine.view().nukes());
        assertEquals(List.of(), engine.view().player(1).skillCards().cards());
    }

    // "Launch yourself in a viper. You may then take 1 more action": the launched pilot plays a card instead of
    // activating his viper (rule 11.2.1).
    @Test
    void theHangarDecksExtraActionMayBeACard() {
        start(all(setUp(KARA_STARBUCK_THRACE), single(new GiveSkillCardEvent(1, BUILD_NUKE_6)), single(new ActionStepEvent()),
                CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new HangarDeckEvent(1)));
        play(engine -> engine.submit(1, new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_4_OCLOCK, KARA_STARBUCK_THRACE, null)));

        assertWaitingFor(new AwaitingChoice(1, Set.of(
                single(new PlayerDecisionEvent<>(1, ActivateViperEvent.class)),
                single(new PlayerDecisionEvent<>(1, ActionEvent.class)),
                single(new NoOpEvent(1)))));
        act(1);
        play(engine -> engine.submit(1, new BuildNukeEvent(1, BUILD_NUKE_6)));

        assertEquals(new Finished(CYLONS), engine.status(1));
        assertEquals(3, engine.view().nukes());
        assertTrue(engine.view().boards().galactica().pilotedFighter(KARA_STARBUCK_THRACE).isPresent());
    }

    // The engine accepts a card only where a decision of its timing is asked.
    @Test
    void aCardAnswersOnlyADecisionOfItsTiming() {
        start(all(setUp(WILLIAM_ADAMA), all(new GiveSkillCardEvent(1, CONSOLIDATE_POWER_1), new GiveSkillCardEvent(1, BUILD_NUKE_6)),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new ConsolidatePowerEvent(1, CONSOLIDATE_POWER_1)));

        assertThrows(InvalidActionException.class, () -> engine.submit(1, new BuildNukeEvent(1, BUILD_NUKE_6)));
    }

    /* the game */

    // Player 1 is the current player, with an empty hand. Each of the three plays a character of another type, as the
    // selection allows (rule A3.7.2).
    private static Followup setUp(Character first) {
        return all(
                new SelectCharacterEvent(1, first),
                new SelectCharacterEvent(2, LAURA_ROSLIN),
                new SelectCharacterEvent(3, first == WILLIAM_ADAMA ? KARA_STARBUCK_THRACE : WILLIAM_ADAMA));
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
        val choice = (AwaitingChoice) engine.pendingInput().orElseThrow();
        val option = choice.options().stream()
                .filter(followup -> followup.equals(single(new PlayerDecisionEvent<>(player, ActionEvent.class))))
                .findFirst()
                .orElseThrow();
        play(engine -> engine.choose(player, option));
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

    private static Set<SkillCardColor> colors(List<SkillCard> cards) {
        return cards.stream().map(card -> card.type().color()).collect(toSet());
    }

    // The view is the game itself.
    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
