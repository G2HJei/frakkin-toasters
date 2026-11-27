package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.CylonPowerReduceMoraleEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.CylonPowerSendToBrigEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.DealLoyaltyCardsEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.GiveLoyaltyCardsEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealCylonEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealCylonOnExecutionEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
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
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.STATE_OF_EMERGENCY;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_DOWN_TO_3;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.LOYALTY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

/**
 * The Cylon reveal (rule 6.1.1) and an executed Cylon's reveal (rule 7.1). The Loyalty Cards are dealt from a stacked
 * Loyalty deck, which a seeded game can't choose. Kara "Starbuck" Thrace is player 1 and the current player, William
 * Adama (Admiral) player 2, Laura Roslin (President) player 3.
 */
class CylonRevealTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private GameEngine engine;

    // The power, then the reveal steps: the "Resurrection Ship", a Super Crisis Card, and the end of her turn.
    @Test
    void aHiddenCylonRevealsAsHerAction() {
        start(all(setUp(), deal(1, CYLON_SEND_TO_BRIG), single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new RevealCylonEvent(1, CYLON_SEND_TO_BRIG)));
        // William Adama is on Galactica, Laura Roslin on Colonial One.
        choose(1, single(new CylonPowerSendToBrigEvent(1, 2)));

        assertTrue(game().player(1).isRevealedCylon());
        assertEquals(RESURRECTION_SHIP, game().locate(KARA_STARBUCK_THRACE).orElseThrow());
        assertEquals(1, game().player(1).superCrisisCards().size());
        assertEquals(BRIG, game().locate(WILLIAM_ADAMA).orElseThrow());
        assertTrue(game().turn().endedEarly());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // "Draw Treachery Cards": she discards down to 3 first, then the humans trade a card and she draws 2 Treachery.
    @Test
    void theDiscardsComeBeforeThePower() {
        val hand = List.of(new SkillCard(1, EVASIVE_MANOEUVRES), new SkillCard(2, EVASIVE_MANOEUVRES),
                new SkillCard(3, LAUNCH_SCOUT), new SkillCard(4, LAUNCH_SCOUT), new SkillCard(5, MAXIMUM_FIREPOWER));
        start(all(setUp(), deal(1, CYLON_DRAW_TREACHERY_CARDS), give(1, hand), single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new RevealCylonEvent(1, CYLON_DRAW_TREACHERY_CARDS)));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_DOWN_TO_3)));
        play(engine -> engine.submit(1, new DiscardSkillCardsEvent(1, hand.subList(0, 2))));

        val kara = game().player(1);
        assertEquals(5, kara.skillCards().size());
        assertEquals(2, kara.skillCards().cards().stream().filter(card -> card.type().color() == TREACHERY).count());
        assertEquals(1, game().player(2).skillCards().size());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // A Cylon may reveal with an action another player's card gave him. That player's turn goes on.
    @Test
    void aCylonRevealsOnAnotherPlayersTurn() {
        start(all(setUp(), deal(2, CYLON_REDUCE_MORALE),
                single(new GrantEvent(2, ACTION, STATE_OF_EMERGENCY)), CYLONS_WIN));
        val morale = engine.view().boards().galactica().morale();

        act(2);
        play(engine -> engine.submit(2, new RevealCylonEvent(2, CYLON_REDUCE_MORALE)));
        choose(2, single(new CylonPowerReduceMoraleEvent(2)));

        assertTrue(game().player(2).isRevealedCylon());
        assertEquals(morale - 1, engine.view().boards().galactica().morale());
        assertFalse(game().turn().endedEarly());
        // He lost the Admiral title to the next in line.
        assertNotEquals(WILLIAM_ADAMA, game().admiral());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // An executed Cylon chooses the card he reveals, and gives the other to a human.
    @Test
    void anExecutedCylonChoosesTheCardHeReveals() {
        start(all(setUp(), deal(1, CYLON_DAMAGE_GALACTICA, CYLON_REDUCE_MORALE), single(new ExecuteEvent(1)), CYLONS_WIN));

        choose(1, single(new RevealCylonOnExecutionEvent(1, CYLON_REDUCE_MORALE)));
        choose(1, single(new GiveLoyaltyCardsEvent(1, 3)));

        assertEquals(List.of(CYLON_REDUCE_MORALE), game().player(1).loyaltyCards().revealedCards());
        assertEquals(RESURRECTION_SHIP, game().locate(KARA_STARBUCK_THRACE).orElseThrow());
        assertTrue(game().player(1).superCrisisCards().isEmpty());
        assertTrue(game().player(3).loyaltyCards().cards().contains(CYLON_DAMAGE_GALACTICA));
        assertEquals(new Finished(CYLONS), engine.status(1));
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
                new MoveCharacterEvent(LAURA_ROSLIN, PRESIDENTS_OFFICE));
    }

    private static Followup deal(int playerNumber, LoyaltyCard... cards) {
        return all(new StackDeckEvent(LOYALTY, List.of(cards)), new DealLoyaltyCardsEvent(playerNumber, cards.length, false));
    }

    private static Followup give(int playerNumber, List<SkillCard> cards) {
        return all(cards.stream().map(card -> single(new GiveSkillCardEvent(playerNumber, card))).toArray(Followup[]::new));
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
        choose(player, single(new PlayerDecisionEvent<>(player, ActionEvent.class)));
    }

    private void choose(int player, Followup option) {
        val choice = (PendingInput.AwaitingChoice) engine.pendingInput().orElseThrow();
        assertTrue(choice.options().contains(option), choice.options().toString());
        play(engine -> engine.choose(player, option));
    }

    private Game game() {
        return (Game) engine.view();
    }

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
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
