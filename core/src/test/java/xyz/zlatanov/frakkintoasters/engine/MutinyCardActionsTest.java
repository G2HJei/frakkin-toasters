package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateUnmannedViperEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.ViperActivation;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.FeedThePeopleEvent;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.PanicEvent;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.SelfishActEvent;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.SetTheAgendaChoiceEvent;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.SetTheAgendaEvent;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.UnauthorizedUsageEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.GiveMutinyCardToPlayerEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.GiveQuorumCardToPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActionStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.*;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.EULOGY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;

/**
 * Kara "Starbuck" Thrace, player 1, gets her cards with {@link GiveMutinyCardToPlayerEvent}, which a seeded game can't
 * choose. Laura Roslin is President and William Adama Admiral.
 */
class MutinyCardActionsTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private GameEngine engine;

    // In "Sickbay" without a Skill Card she has no other action: the card is what the Action step offers.
    @Test
    void aMutinyCardIsAnAction() {
        start(all(setUp(), single(new MoveCharacterEvent(KARA_STARBUCK_THRACE, SICKBAY)), give(FEED_THE_PEOPLE),
                single(new ActionStepEvent()), CYLONS_WIN));
        val food = galactica().food();

        act(1);
        play(engine -> engine.submit(1, new FeedThePeopleEvent(1)));

        assertEquals(food + 1, galactica().food());
        assertTrue(game().removedComponents().contains(FEED_THE_PEOPLE));
        assertTrue(game().player(1).mutinyCards().isEmpty());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // Mutiny Cards are played in the "Brig" too (rule 5.2.3.2), and the new one doesn't send her anywhere.
    @Test
    void selfishActInTheBrigDrawsAnotherMutinyCard() {
        start(all(setUp(), single(new MoveCharacterEvent(KARA_STARBUCK_THRACE, BRIG)), give(SELFISH_ACT),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new SelfishActEvent(1)));
        play(engine -> engine.submit(1, new DrawSkillCardsEvent(1, Map.of(TACTICS, 2))));

        assertEquals(2, game().player(1).skillCards().size());
        assertEquals(1, game().player(1).mutinyCards().size());
        assertEquals(BRIG, game().locate(KARA_STARBUCK_THRACE).orElseThrow());
        assertTrue(game().decks().mutiny().discardedCards().contains(SELFISH_ACT));
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    @Test
    void setTheAgendaPutsTwoQuorumCardsAtTheBottom() {
        start(all(setUp(), single(new GiveQuorumCardToPresidentEvent(EULOGY)), give(SET_THE_AGENDA),
                single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new SetTheAgendaEvent(1)));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, SetTheAgendaChoiceEvent.class)));
        val hand = game().presidentHand().cards();
        assertEquals(3, hand.size());
        play(engine -> engine.submit(1, new SetTheAgendaChoiceEvent(1, List.of(hand.get(0), hand.get(1)))));

        assertEquals(List.of(hand.get(2)), game().presidentHand().cards());
        val deck = game().decks().quorum().cards();
        assertEquals(List.of(hand.get(0), hand.get(1)), deck.subList(deck.size() - 2, deck.size()));
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    @Test
    void panicPlacesACivilianShipThenActivatesAnUnmannedViper() {
        start(all(setUp(), give(PANIC), single(new ActionStepEvent()), CYLONS_WIN));
        val civilianShips = galactica().shipsIn(GALACTICA_SPACE_3_OCLOCK, CivilianShip.class).size();

        act(1);
        play(engine -> engine.submit(1, new PanicEvent(1)));
        assertEquals(civilianShips + 1, galactica().shipsIn(GALACTICA_SPACE_3_OCLOCK, CivilianShip.class).size());
        play(engine -> engine.submit(1, new ActivateUnmannedViperEvent(1, ViperActivation.launch(VIPER, GALACTICA_SPACE_7_OCLOCK))));

        assertEquals(1, galactica().shipsIn(GALACTICA_SPACE_7_OCLOCK, xyz.zlatanov.frakkintoasters.state.ship.Viper.class).size());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    @Test
    void unauthorizedUsageSpendsEveryNuke() {
        start(all(setUp(), give(UNAUTHORIZED_USAGE), single(new ActionStepEvent()), CYLONS_WIN));

        act(1);
        play(engine -> engine.submit(1, new UnauthorizedUsageEvent(1, GALACTICA_SPACE_11_OCLOCK)));

        assertEquals(0, engine.view().nukes());
        assertTrue(game().removedComponents().contains(UNAUTHORIZED_USAGE));
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    @Test
    void aCardThatCantBePlayedIsRejected() {
        start(all(setUp(), give(MAKE_A_DEAL), single(new ActionStepEvent()), CYLONS_WIN));

        act(1);

        assertThrows(xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException.class,
                () -> engine.submit(1, new xyz.zlatanov.frakkintoasters.event.action.mutiny.MakeADealEvent(1, 2, COMMAND, 1)));
        assertRestoresTheSameGame();
    }

    private static Followup setUp() {
        return all(
                new SelectCharacterEvent(1, KARA_STARBUCK_THRACE),
                new SelectCharacterEvent(2, WILLIAM_ADAMA),
                new SelectCharacterEvent(3, LAURA_ROSLIN),
                new MakePresidentEvent(LAURA_ROSLIN),
                new MakeAdmiralEvent(WILLIAM_ADAMA));
    }

    private static Followup give(MutinyCard card) {
        return single(new GiveMutinyCardToPlayerEvent(1, card));
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
        val choice = (AwaitingChoice) engine.pendingInput().orElseThrow();
        assertTrue(choice.options().contains(option), choice.options().toString());
        play(engine -> engine.choose(player, option));
    }

    private Game game() {
        return (Game) engine.view();
    }

    private GalacticaBoardView galactica() {
        return engine.view().boards().galactica();
    }

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
