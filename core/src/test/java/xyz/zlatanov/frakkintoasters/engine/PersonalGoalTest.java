package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.CreateLoyaltyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealPersonalGoalEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.GiveLoyaltyCardToPlayerEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActionStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON_SELFISH;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON_SELF_DESTRUCTION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

/**
 * Revealing a Personal Goal as an action (rule 5.3.1). Kara "Starbuck" Thrace is player 1 and the current player,
 * William Adama (Admiral) player 2, Laura Roslin (President) player 3.
 */
class PersonalGoalTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private GameEngine engine;

    // In "Sickbay" she reveals "Self Destruction", and before 7 distance she draws a new Loyalty Card.
    @Test
    void aPersonalGoalIsRevealedAndANewLoyaltyCardDrawn() {
        start(all(setUp(), give(NOT_CYLON_SELF_DESTRUCTION), single(new MoveCharacterEvent(KARA_STARBUCK_THRACE, SICKBAY)),
                single(new ActionStepEvent()), CYLONS_WIN));
        val loyaltyCards = game().player(1).loyaltyCards().size();

        act(1);
        play(engine -> engine.submit(1, new RevealPersonalGoalEvent(1, NOT_CYLON_SELF_DESTRUCTION, List.of())));

        val kara = game().player(1);
        assertEquals(List.of(NOT_CYLON_SELF_DESTRUCTION), kara.loyaltyCards().revealedCards());
        assertEquals(loyaltyCards, kara.loyaltyCards().size()); // the revealed card is replaced
        assertEquals(EngineStatus.finished(CYLONS), engine.status(1));
    }

    // "Selfish" discards Skill Cards worth 20 strength to reveal it.
    @Test
    void selfishIsRevealedByDiscardingTwentyStrength() {
        val hand = List.of(new SkillCard(5, EXECUTIVE_ORDER), new SkillCard(6, EXECUTIVE_ORDER),
                new SkillCard(4, LAUNCH_SCOUT), new SkillCard(5, LAUNCH_SCOUT));
        start(all(setUp(), give(NOT_CYLON_SELFISH), all(hand.stream()
                        .map(card -> single(new GiveSkillCardEvent(1, card)))
                        .toArray(Followup[]::new)),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new RevealPersonalGoalEvent(1, NOT_CYLON_SELFISH, hand)));

        val kara = game().player(1);
        assertTrue(kara.loyaltyCards().revealedCards().contains(NOT_CYLON_SELFISH));
        assertTrue(kara.skillCards().isEmpty());
        assertEquals(EngineStatus.finished(CYLONS), engine.status(1));
    }

    private static Followup setUp() {
        return all(
                new SelectCharacterEvent(1, KARA_STARBUCK_THRACE),
                new SelectCharacterEvent(2, WILLIAM_ADAMA),
                new SelectCharacterEvent(3, LAURA_ROSLIN),
                new MakePresidentEvent(LAURA_ROSLIN),
                new MakeAdmiralEvent(WILLIAM_ADAMA),
                new MoveCharacterEvent(KARA_STARBUCK_THRACE, HANGAR_DECK),
                new MoveCharacterEvent(WILLIAM_ADAMA, COMMAND),
                new MoveCharacterEvent(LAURA_ROSLIN, PRESIDENTS_OFFICE),
                new CreateLoyaltyDeckEvent());
    }

    private static Followup give(LoyaltyCard card) {
        return single(new GiveLoyaltyCardToPlayerEvent(1, card));
    }

    private void start(Followup script) {
        engine = new GameEngine(3, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    private void act(int player) {
        val option = single(new PlayerDecisionEvent<>(player, ActionEvent.class));
        val choice = (PendingInput.AwaitingChoice) engine.pendingInput().orElseThrow();
        assertTrue(choice.options().contains(option), choice.options().toString());
        play(engine -> engine.choose(player, option));
    }

    private Game game() {
        return (Game) engine.view();
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
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
