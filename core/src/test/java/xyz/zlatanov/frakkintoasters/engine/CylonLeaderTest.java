package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.quorum.ProbationEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.location.cylon.HumanFleetInfiltrateEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.ReturnToResurrectionShipEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.GiveMutinyCardToPlayerEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.GiveQuorumCardToPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActionStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_DOWN_TO_3;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.PANIC;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.PROBATION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

/**
 * An Infiltrating Cylon Leader's return to the "Resurrection Ship" (rule 6.2.3.5), played through {@code submit} and
 * {@code choose} alone, restoring the game at every pause. Caprica Six is player 1 and the current player, Kara
 * "Starbuck" Thrace player 2, William Adama (Admiral) player 3, Laura Roslin (President) player 4.
 */
class CylonLeaderTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private static final List<SkillCard> HAND = List.of(new SkillCard(1, EVASIVE_MANOEUVRES),
            new SkillCard(2, LAUNCH_SCOUT), new SkillCard(3, REPAIR), new SkillCard(4, REPAIR),
            new SkillCard(5, MAXIMUM_FIREPOWER));

    private GameEngine engine;

    // She takes the Infiltration Card's action from the "Brig": the Quorum Card given to her and her Mutiny Card go,
    // then she discards down to 3 Skill Cards. She keeps her miracle token.
    @Test
    void anInfiltratingCylonLeaderReturnsFromTheBrig() {
        start(all(setUp(), infiltrate(BRIG), give(HAND), single(new GiveMutinyCardToPlayerEvent(1, PANIC)),
                all(new GiveQuorumCardToPresidentEvent(PROBATION), new ProbationEvent(4, 1)),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new ReturnToResurrectionShipEvent(1)));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_DOWN_TO_3)));
        play(engine -> engine.submit(1, new DiscardSkillCardsEvent(1, HAND.subList(0, 2))));

        val caprica = game().player(1);
        assertFalse(caprica.isInfiltrating());
        assertEquals(RESURRECTION_SHIP, game().locate(CAPRICA_SIX).orElseThrow());
        assertEquals(Set.copyOf(HAND.subList(2, 5)), Set.copyOf(caprica.skillCards().cards()));
        assertTrue(caprica.mutinyCards().isEmpty());
        assertEquals(List.of(), game().quorumCardsInPlay());
        assertTrue(caprica.hasMiracleToken());
        assertEquals(EngineStatus.finished(CYLONS), engine.status(1));
    }

    // Outside the "Brig" she keeps her hand.
    @Test
    void anInfiltratingCylonLeaderReturnsFromGalactica() {
        start(all(setUp(), infiltrate(COMMAND), give(HAND), single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new ReturnToResurrectionShipEvent(1)));

        assertFalse(game().player(1).isInfiltrating());
        assertEquals(RESURRECTION_SHIP, game().locate(CAPRICA_SIX).orElseThrow());
        assertEquals(Set.copyOf(HAND), Set.copyOf(game().player(1).skillCards().cards()));
        assertEquals(EngineStatus.finished(CYLONS), engine.status(1));
    }

    private static Followup setUp() {
        return all(
                new SelectCharacterEvent(1, CAPRICA_SIX),
                new SelectCharacterEvent(2, KARA_STARBUCK_THRACE),
                new SelectCharacterEvent(3, WILLIAM_ADAMA),
                new SelectCharacterEvent(4, LAURA_ROSLIN),
                new MakePresidentEvent(LAURA_ROSLIN),
                new MakeAdmiralEvent(WILLIAM_ADAMA));
    }

    // Caprica Six infiltrates at the "Human Fleet", then goes where the test wants her.
    private static Followup infiltrate(Location location) {
        return all(
                new MoveCharacterEvent(CAPRICA_SIX, HUMAN_FLEET),
                new HumanFleetInfiltrateEvent(1, COMMAND),
                new MoveCharacterEvent(CAPRICA_SIX, location));
    }

    private static Followup give(List<SkillCard> cards) {
        return all(cards.stream().map(card -> single(new GiveSkillCardEvent(1, card))).toArray(Followup[]::new));
    }

    private void start(Followup script) {
        engine = new GameEngine(4, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    // The player chooses to take an action.
    private void act(int player) {
        val option = single(new PlayerDecisionEvent<>(player, ActionEvent.class));
        val choice = (PendingInput.AwaitingChoice) engine.pendingInput().orElseThrow();
        assertTrue(choice.options().contains(option), choice.options().toString());
        play(engine -> engine.choose(player, option));
    }

    private Game game() {
        return (Game) engine.view();
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

    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 4).mapToObj(engine::status).toList();
    }
}
