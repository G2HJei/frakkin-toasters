package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.FinalFiveCardShown;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.ResolveFinalFiveCardEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.ResolveFinalFiveCardsEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.*;
import xyz.zlatanov.frakkintoasters.event.sandbox.GiveLoyaltyCardToPlayerEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
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
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.LOYALTY;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.LOYALTY_NOT_CYLON;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

/**
 * Final Five cards resolving through the engine, restoring at every pause: another player's look (rule 5.4.1.1) and an
 * execution (rule 5.4.1.2).
 */
class FinalFiveTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private GameEngine engine;

    // The player who looked at "You Are Sent to Brig" goes to the "Brig". The holder keeps the card.
    @Test
    void aLookSendsTheLookerToTheBrig() {
        start(all(setUp(), give(2, FINAL_FIVE_YOU_ARE_SENT_TO_BRIG), single(new LookAtLoyaltyCardEvent(1, 2)), CYLONS_WIN));

        assertEquals(BRIG, game().locate(KARA_STARBUCK_THRACE).orElseThrow());
        assertEquals(List.of(FINAL_FIVE_YOU_ARE_SENT_TO_BRIG), game().player(2).loyaltyCards().cards());
        assertTrue(engine.journal().facts().contains(new FinalFiveCardShown(2, FINAL_FIVE_YOU_ARE_SENT_TO_BRIG)));
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // A look at all of a player's cards finds two Final Five cards: the current player picks the order.
    @Test
    void theCurrentPlayerOrdersSeveralCards() {
        start(all(setUp(), give(2, FINAL_FIVE_DAMAGE_GALACTICA_TWICE, FINAL_FIVE_YOU_ARE_SENT_TO_BRIG),
                single(new LookAtAllLoyaltyCardsEvent(3, 2)), CYLONS_WIN));

        choose(1, all(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_YOU_ARE_SENT_TO_BRIG, 3),
                new ResolveFinalFiveCardsEvent(2, List.of(FINAL_FIVE_DAMAGE_GALACTICA_TWICE), 3)));
        resolveDamage();

        assertEquals(BRIG, game().locate(LAURA_ROSLIN).orElseThrow());
        val shown = engine.journal().facts().stream().filter(FinalFiveCardShown.class::isInstance).toList();
        assertEquals(List.of(new FinalFiveCardShown(2, FINAL_FIVE_YOU_ARE_SENT_TO_BRIG),
                new FinalFiveCardShown(2, FINAL_FIVE_DAMAGE_GALACTICA_TWICE)), shown);
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // "You Are Executed": the holder is executed, the card leaves the game, the others discard 2 random Skill Cards.
    @Test
    void aLookExecutesTheHolder() {
        start(all(setUp(), give(2, FINAL_FIVE_YOU_ARE_EXECUTED), giveSkillCards(1, 3), giveSkillCards(3, 3),
                notACylonCard(), single(new LookAtLoyaltyCardEvent(1, 2)), CYLONS_WIN));

        play(engine -> engine.submit(2, new SelectCharacterEvent(2, LOUIS_HOSHI)));

        assertTrue(game().removedComponents().contains(WILLIAM_ADAMA));
        assertTrue(game().removedComponents().contains(FINAL_FIVE_YOU_ARE_EXECUTED));
        assertEquals(1, game().player(1).skillCards().size());
        assertEquals(1, game().player(3).skillCards().size());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // An execution reveals a Final Five card: every other human discards 2 Skill Cards, and the card leaves the game.
    @Test
    void anExecutionRevealsACard() {
        start(all(setUp(), give(2, FINAL_FIVE_THEY_ARE_EXECUTED), giveSkillCards(1, 3), giveSkillCards(3, 3),
                notACylonCard(), single(new ExecuteEvent(2)), CYLONS_WIN));

        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_EXACTLY_2)));
        play(engine -> engine.submit(1, new DiscardSkillCardsEvent(1, game().player(1).skillCards().cards().subList(0, 2))));
        play(engine -> engine.submit(3, new DiscardSkillCardsEvent(3, game().player(3).skillCards().cards().subList(0, 2))));
        play(engine -> engine.submit(2, new SelectCharacterEvent(2, LOUIS_HOSHI)));

        assertTrue(game().removedComponents().contains(FINAL_FIVE_THEY_ARE_EXECUTED));
        assertEquals(1, game().player(1).skillCards().size());
        assertEquals(1, game().player(3).skillCards().size());
        assertFalse(engine.journal().facts().stream().anyMatch(FinalFiveCardShown.class::isInstance));
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

    // The sandbox's setup event, out of the Loyalty deck.
    private static Followup give(int playerNumber, LoyaltyCard... cards) {
        return all(single(new StackDeckEvent(LOYALTY, List.of(cards))), all(List.of(cards).stream()
                .map(card -> single(new GiveLoyaltyCardToPlayerEvent(playerNumber, card)))
                .toArray(Followup[]::new)));
    }

    private static Followup giveSkillCards(int playerNumber, int count) {
        return all(IntStream.range(0, count)
                .mapToObj(i -> single(new GiveSkillCardEvent(playerNumber, new SkillCard(10 * playerNumber + i, EVASIVE_MANOEUVRES))))
                .toArray(Followup[]::new));
    }

    // The executed player's new character draws a Loyalty Card after a "You Are Not a Cylon" card is added (rule 7.1).
    private static Followup notACylonCard() {
        return single(new StackDeckEvent(LOYALTY_NOT_CYLON, List.of(NOT_CYLON)));
    }

    // Each Galactica damage token is resolved by its own choice, if any.
    private void resolveDamage() {
        while (engine.pendingInput().orElse(null) instanceof PendingInput.AwaitingChoice choice) {
            choose(choice.player(), choice.options().iterator().next());
        }
    }

    private void start(Followup script) {
        engine = new GameEngine(3, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    private void choose(int player, Followup option) {
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
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
