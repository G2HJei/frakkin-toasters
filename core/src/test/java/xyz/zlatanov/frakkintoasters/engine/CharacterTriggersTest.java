package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.EnactProductionQuotasEvent;
import xyz.zlatanov.frakkintoasters.event.character.AdaptableEvent;
import xyz.zlatanov.frakkintoasters.event.character.ForLoveEvent;
import xyz.zlatanov.frakkintoasters.event.character.ForcedDiscardEvent;
import xyz.zlatanov.frakkintoasters.event.character.ManipulativeEvent;
import xyz.zlatanov.frakkintoasters.event.character.MementoEvent;
import xyz.zlatanov.frakkintoasters.event.character.NecessaryStepsEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.GiveQuorumCardToPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.turn.ActionStepEvent;
import xyz.zlatanov.frakkintoasters.event.turn.EndTurnEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_EXACTLY_1;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2_OF_ANY_COLOR;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.CHOSEN;
import static xyz.zlatanov.frakkintoasters.state.Title.ADMIRAL;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.BLACKMAIL;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.PANIC;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ENACT_PRODUCTION_QUOTAS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MEMENTO;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

/**
 * The character abilities that answer something wherever it happens, through {@code submit} and {@code choose},
 * restoring at every pause. Player 1 is the current player. Each script ends the game after its events.
 */
class CharacterTriggersTest {

    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private GameEngine engine;
    private int        players;

    // "Memento": the engine offers it right after the event that lost population.
    @Test
    void hotDogDrawsAfterPopulationIsLost() {
        start(3, setUp(LAURA_ROSLIN, BRENDAN_HOTDOG_COSTANZA, SAUL_TIGH), single(new LoseResourceEvent(POPULATION, 1)));

        assertWaitingFor(new AwaitingChoice(2, Set.of(single(new MementoEvent(2)), single(new NoOpEvent(2)))));
        play(engine -> engine.choose(2, single(new MementoEvent(2))));
        val drawn = engine.view().player(2).skillCards().cards();
        assertEquals(3, drawn.size());
        play(engine -> engine.choose(2, single(new DiscardSkillCardsEvent(2, List.of(drawn.getFirst())))));

        assertEquals(2, engine.view().player(2).skillCards().size());
        assertTrue(engine.view().turn().hasUsed(2, MEMENTO));
        assertEquals(EngineStatus.finished(Team.CYLONS), engine.status(1));
    }

    // "For Love": Sharon "Athena" Agathon draws a Treachery card so that Laura Roslin discards 1 card, not 2.
    @Test
    void athenaMakesADiscardSmaller() {
        val first = new SkillCard(1, CONSOLIDATE_POWER);
        val second = new SkillCard(2, CONSOLIDATE_POWER);
        start(4, all(setUp(LAURA_ROSLIN, SHARON_ATHENA_AGATHON, SAUL_TIGH, KARA_STARBUCK_THRACE),
                        all(new GiveSkillCardEvent(1, first), new GiveSkillCardEvent(1, second))),
                single(new ForcedDiscardEvent(1, CHOSEN, 2, false)));

        assertWaitingFor(new AwaitingChoice(2, Set.of(single(new ForLoveEvent(2, 1, CHOSEN, 2)),
                single(new ForcedDiscardEvent(1, CHOSEN, 2, true)))));
        play(engine -> engine.choose(2, single(new ForLoveEvent(2, 1, CHOSEN, 2))));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_EXACTLY_1)));
        play(engine -> engine.submit(1, new DiscardSkillCardsEvent(1, List.of(first))));

        assertEquals(List.of(second), engine.view().player(1).skillCards().cards());
        assertEquals(TREACHERY, engine.view().player(2).skillCards().cards().getFirst().type().color());
        assertEquals(EngineStatus.finished(Team.CYLONS), engine.status(1));
    }

    // "Necessary Steps": Tom Zarek (alt.) picks the Mutiny Card Laura Roslin draws, from the top 2.
    @Test
    void zarekPicksTheMutinyCard() {
        start(3, setUp(LAURA_ROSLIN, TOM_ZAREK_ALT, KARA_STARBUCK_THRACE), all(
                single(new StackDeckEvent(DeckId.MUTINY, List.of(BLACKMAIL, PANIC))),
                single(new DrawMutinyCardEvent(1))));
        // his own starting card ("Disreputable") came the same way
        answerZareksStartingCard();

        assertWaitingFor(new AwaitingChoice(2, Set.of(single(new NecessaryStepsEvent(2, 1, BLACKMAIL, PANIC, false)),
                single(new NecessaryStepsEvent(2, 1, PANIC, BLACKMAIL, false)))));
        play(engine -> engine.choose(2, single(new NecessaryStepsEvent(2, 1, PANIC, BLACKMAIL, false))));

        assertEquals(List.of(PANIC), engine.view().player(1).mutinyCards().cards());
        assertEquals(BLACKMAIL, engine.view().decks().deck(DeckId.MUTINY).bottom());
        assertEquals(EngineStatus.finished(Team.CYLONS), engine.status(1));
    }

    // "Adaptable": after Laura Roslin plays a Quorum Card, Tory Foster draws 2 Skill Cards of any types.
    @Test
    void toryDrawsAfterAQuorumCard() {
        start(4, all(setUp(LAURA_ROSLIN, SAUL_TIGH, KARA_STARBUCK_THRACE, TORY_FOSTER),
                single(new GiveQuorumCardToPresidentEvent(ENACT_PRODUCTION_QUOTAS))), single(new ActionStepEvent()));

        val action = (AwaitingChoice) engine.pendingInput().orElseThrow();
        play(engine -> engine.choose(1, action.options().stream()
                .filter(option -> option.equals(single(new PlayerDecisionEvent<>(1, ActionEvent.class))))
                .findFirst().orElseThrow()));
        play(engine -> engine.submit(1, new EnactProductionQuotasEvent(1)));
        assertWaitingFor(new AwaitingChoice(4, Set.of(single(new AdaptableEvent(4)), single(new NoOpEvent(4)))));
        play(engine -> engine.choose(4, single(new AdaptableEvent(4))));
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(4, DrawSkillCardsEvent.class, DRAW_EXACTLY_2_OF_ANY_COLOR)));
        val hand = engine.view().player(4).skillCards().size();
        play(engine -> engine.submit(4, new DrawSkillCardsEvent(4, Map.of(POLITICS, 2))));

        assertEquals(hand + 2, engine.view().player(4).skillCards().size());
        assertEquals(EngineStatus.finished(Team.CYLONS), engine.status(1));
    }

    // "Manipulative": Ellen Tigh is Admiral for her turn, and Saul Tigh is again once it ends.
    @Test
    void ellenTighBorrowsTheAdmiralsTitle() {
        start(3, setUp(ELLEN_TIGH, SAUL_TIGH, KARA_STARBUCK_THRACE), all(
                one(new ManipulativeEvent(1, ADMIRAL), new NoOpEvent(1)),
                single(new EndTurnEvent())));
        assertEquals(SAUL_TIGH, engine.view().admiral());

        play(engine -> engine.choose(1, single(new ManipulativeEvent(1, ADMIRAL))));

        // the end of her turn gave it back, and Saul Tigh's turn began
        assertEquals(SAUL_TIGH, engine.view().admiral());
        assertEquals(0, engine.view().player(1).miracleTokens());
        assertEquals(2, engine.view().turn().currentPlayerNumber());
    }

    /* inputs */

    private void answerZareksStartingCard() {
        val choice = (AwaitingChoice) engine.pendingInput().orElseThrow();
        assertInstanceOf(NecessaryStepsEvent.class, ((Followup.Single) choice.options().iterator().next()).event());
        play(engine -> engine.choose(2, choice.options().iterator().next()));
    }

    // The characters by seat, then the titles.
    private static Followup setUp(Character... characters) {
        val events = new ArrayList<Event>();
        for (int i = 0; i < characters.length; i++) {
            events.add(new SelectCharacterEvent(i + 1, characters[i]));
        }
        events.add(new DetermineNextPresidentEvent());
        events.add(new DetermineNextAdmiralEvent());
        events.add(new DetermineNextCagEvent());
        return all(events.toArray(Event[]::new));
    }

    private void start(int playerCount, Followup setUp, Followup events) {
        players = playerCount;
        engine = new GameEngine(playerCount, Randomness.seeded(42), new ScriptEvent(all(setUp, events, CYLONS_WIN)));
        assertRestoresTheSameGame();
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

    private List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, players).mapToObj(engine::status).toList();
    }
}
