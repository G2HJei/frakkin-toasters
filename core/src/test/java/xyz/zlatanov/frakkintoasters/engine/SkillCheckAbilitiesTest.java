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
import xyz.zlatanov.frakkintoasters.event.character.*;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.AdmiralsQuartersEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.BrigEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LeaveBrigEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.*;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.LEADERSHIP;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;

/**
 * The cards that decide each check are dealt, not drawn.
 */
class SkillCheckAbilitiesTest {

    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    // The top of the Destiny deck: a Politics and a Tactics 1, neither with an ability.
    private static final List<SkillCard> DESTINY = List.of(new SkillCard(1, CONSOLIDATE_POWER), new SkillCard(1, LAUNCH_SCOUT));

    private GameEngine                     engine;
    private int                            players;
    private Map<Integer, List<SkillCard>>  intoTheCheck;

    // Dee takes the top 3 Leadership cards into a "Brig" check ("Fast Learner"), the Chief makes Leadership count 0
    // ("Blind Devotion"), so the Politics and Tactics check passes, and William Adama takes its cards ("Command
    // Authority").
    @Test
    void fastLearnerBlindDevotionAndCommandAuthority() {
        val executiveOrders = List.of(new SkillCard(1, EXECUTIVE_ORDER), new SkillCard(2, EXECUTIVE_ORDER),
                new SkillCard(3, EXECUTIVE_ORDER));
        val politics = new SkillCard(3, CONSOLIDATE_POWER);
        val tactics = new SkillCard(3, LAUNCH_SCOUT);
        intoTheCheck = Map.of(1, List.of(), 2, List.of(politics), 3, List.of(tactics));
        start(3, all(
                select(ANASTASIA_DEE_DUALLA, CHIEF_GALEN_TYROL, WILLIAM_ADAMA),
                give(intoTheCheck),
                single(new StackDeckEvent(DeckId.LEADERSHIP, executiveOrders)),
                single(new SendToBrigEvent(1)),
                single(new PlayerDecisionEvent<>(1, ActionEvent.class)),
                CYLONS_WIN));

        play(engine -> engine.submit(1, new BrigEvent(1)));
        answerTheRoutine();

        // "Fast Learner", before any card goes in: a deck, then the check or her hand.
        assertTrue(choiceIsPending(1, new FastLearnerEvent(1, LEADERSHIP)));
        play(engine -> engine.choose(1, single(new FastLearnerEvent(1, LEADERSHIP))));
        assertWaitingFor(new AwaitingChoice(1, Set.of(
                single(new FastLearnerIntoCheckEvent(1, executiveOrders)), single(new NoOpEvent(1)))));
        play(engine -> engine.choose(1, single(new FastLearnerIntoCheckEvent(1, executiveOrders))));
        answerTheRoutine();

        // "Blind Devotion", once everyone put cards in, before they are revealed.
        assertTrue(choiceIsPending(2, new BlindDevotionEvent(2, LEADERSHIP)));
        play(engine -> engine.choose(2, single(new BlindDevotionEvent(2, LEADERSHIP))));
        answerTheRoutine();

        // 1 + 1 + 3 + 3, the Leadership cards count 0 instead of -6: a pass against 7.
        val check = engine.view().activeSkillCheck();
        assertEquals(8, check.totalStrength());
        assertEquals(PASS, check.result());
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, LeaveBrigEvent.class,
                xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DESTINATION_GALACTICA)));
        play(engine -> engine.submit(1, new LeaveBrigEvent(1, COMMAND)));

        // "Command Authority", once the result is resolved: the 7 cards of the check go to William Adama.
        assertWaitingFor(new AwaitingChoice(3, Set.of(single(new CommandAuthorityEvent(3)), single(new NoOpEvent(3)))));
        play(engine -> engine.choose(3, single(new CommandAuthorityEvent(3))));

        assertNull(engine.view().activeSkillCheck());
        val hand = engine.view().player(3).skillCards().cards();
        assertTrue(hand.containsAll(executiveOrders));
        assertTrue(hand.containsAll(List.of(politics, tactics)));
        assertTrue(hand.containsAll(DESTINY));
        assertEquals(0, engine.view().player(3).miracleTokens());
    }

    // Kara Thrace's "Insubordinate" and Saul Tigh's "Cylon Hatred" make the "Admiral's Quarters" 6 easier, and Cally,
    // the current player, discards a revealed card with "Quick Fix".
    @Test
    void insubordinateCylonHatredAndQuickFix() {
        val politics = new SkillCard(2, CONSOLIDATE_POWER);
        intoTheCheck = Map.of(
                1, List.of(politics, new SkillCard(1, LAUNCH_SCOUT)), // "Impulsive": never just 1
                2, List.of(new SkillCard(5, LAUNCH_SCOUT)),
                3, List.of(new SkillCard(4, LAUNCH_SCOUT)),
                4, List.of(new SkillCard(3, EXECUTIVE_ORDER)));
        start(4, all(
                select(CALLANDRA_CALLY_TYROL, SAUL_TIGH, KARA_STARBUCK_THRACE, LAURA_ROSLIN),
                give(intoTheCheck),
                single(new MoveCharacterEvent(CALLANDRA_CALLY_TYROL, ADMIRALS_QUARTERS)),
                single(new PlayerDecisionEvent<>(1, ActionEvent.class)),
                CYLONS_WIN));

        play(engine -> engine.submit(1, new AdmiralsQuartersEvent(1, 3)));

        assertEquals(-3, engine.view().activeSkillCheck().difficultyAdjustment());
        assertWaitingFor(new AwaitingChoice(2, Set.of(single(new CylonHatredEvent(2)), single(new NoOpEvent(2)))));
        play(engine -> engine.choose(2, single(new CylonHatredEvent(2))));
        assertEquals(-6, engine.view().activeSkillCheck().difficultyAdjustment());
        answerTheRoutine();

        // Cally takes out her own Politics card, which wouldn't count in a Leadership and Tactics check.
        assertTrue(choiceIsPending(1, new QuickFixEvent(1, politics)));
        play(engine -> engine.choose(1, single(new QuickFixEvent(1, politics))));
        answerTheRoutine();

        // The check passed: the Politics card is gone.
        assertNull(engine.view().activeSkillCheck());
        assertEquals(Optional.of(BRIG), engine.view().locate(KARA_STARBUCK_THRACE));
        assertTrue(game().decks().politics().discardedCards().contains(politics));
    }

    // Simon O'Neill, a Cylon Leader who isn't Infiltrating, puts 2 cards in instead of 1 ("Calculating") and shows one
    // of them ("Logic-Bound").
    @Test
    void calculatingAndLogicBound() {
        val shown = new SkillCard(2, LAUNCH_SCOUT);
        intoTheCheck = Map.of(
                1, List.of(),
                2, List.of(shown, new SkillCard(2, EXECUTIVE_ORDER)),
                3, List.of(new SkillCard(3, LAUNCH_SCOUT)),
                4, List.of(new SkillCard(1, LAUNCH_SCOUT)));
        start(4, all(
                select(ROMO_LAMPKIN, SIMON_ONEIL, FELIX_GAETA, SHERMAN_DOC_COTTLE),
                give(intoTheCheck),
                single(new MoveCharacterEvent(ROMO_LAMPKIN, ADMIRALS_QUARTERS)),
                single(new PlayerDecisionEvent<>(1, ActionEvent.class)),
                CYLONS_WIN));

        play(engine -> engine.submit(1, new AdmiralsQuartersEvent(1, 3)));
        answerTheRoutine();

        // It isn't optional: he picks the card, nothing else.
        assertWaitingFor(new AwaitingChoice(2, Set.of(
                single(new LogicBoundEvent(2, shown)), single(new LogicBoundEvent(2, new SkillCard(2, EXECUTIVE_ORDER))))));
        play(engine -> engine.choose(2, single(new LogicBoundEvent(2, shown))));
        assertEquals(List.of(shown), engine.view().activeSkillCheck().shownCards(2));
        answerTheRoutine();

        assertNull(engine.view().activeSkillCheck());
        assertEquals(Optional.of(BRIG), engine.view().locate(FELIX_GAETA));
    }

    // "Caprica" Six adds a card from her hand after the reveal ("Human Delusion").
    @Test
    void humanDelusion() {
        val delusion = new SkillCard(4, EXECUTIVE_ORDER);
        intoTheCheck = Map.of(
                1, List.of(),
                2, List.of(new SkillCard(3, LAUNCH_SCOUT)),
                3, List.of(new SkillCard(1, LAUNCH_SCOUT)),
                4, List.of());
        start(4, all(
                select(ROMO_LAMPKIN, CAPRICA_SIX, FELIX_GAETA, SHERMAN_DOC_COTTLE),
                give(intoTheCheck),
                single(new GiveSkillCardEvent(2, delusion)),
                single(new MoveCharacterEvent(ROMO_LAMPKIN, ADMIRALS_QUARTERS)),
                single(new PlayerDecisionEvent<>(1, ActionEvent.class)),
                CYLONS_WIN));

        play(engine -> engine.submit(1, new AdmiralsQuartersEvent(1, 3)));
        answerTheRoutine();

        assertWaitingFor(new AwaitingChoice(2, Set.of(
                single(new PlayerDecisionEvent<>(2, HumanDelusionEvent.class)), single(new NoOpEvent(2)))));
        play(engine -> engine.choose(2, single(new PlayerDecisionEvent<>(2, HumanDelusionEvent.class))));
        play(engine -> engine.submit(2, new HumanDelusionEvent(2, List.of(delusion))));
        assertTrue(engine.view().activeSkillCheck().dividedCards().contains(delusion));
        answerTheRoutine();

        // 3 + 1 + 4, the Destiny cards cancel out: a pass against 7.
        assertNull(engine.view().activeSkillCheck());
        assertEquals(Optional.of(BRIG), engine.view().locate(FELIX_GAETA));
        assertEquals(0, engine.view().player(2).miracleTokens());
    }

    private void answerTheRoutine() {
        while (engine.pendingInput().isPresent()) {
            val input = engine.pendingInput().get();
            if (input instanceof AwaitingDecision(var decision)) {
                val player = decision.playerNumber();
                if (decision.action() == PlayCardsBeforeSkillCheckEvent.class) {
                    play(engine -> engine.submit(player, new PlayCardsBeforeSkillCheckEvent(player)));
                } else if (decision.action() == PlayCardsIntoSkillCheckEvent.class) {
                    play(engine -> engine.submit(player, new PlayCardsIntoSkillCheckEvent(player, intoTheCheck.get(player))));
                } else {
                    return;
                }
            } else if (input instanceof AwaitingChoice(var chooser, var options) && isRoutine(chooser, options)) {
                play(engine -> engine.choose(chooser, single(new NoOpEvent(chooser))));
            } else {
                return;
            }
        }
    }

    private static boolean isRoutine(int chooser, Set<Followup> options) {
        return options.contains(single(new NoOpEvent(chooser))) && options.stream()
                .filter(option -> !option.equals(single(new NoOpEvent(chooser))))
                .allMatch(option -> option instanceof Followup.Single(PlayerDecisionEvent<?> decision)
                        && (decision.action() == PoliticalProwessEvent.class
                        || decision.action() == DeclareEmergencyEvent.class
                        || decision.action() == ChangeOfPlansEvent.class));
    }

    private boolean choiceIsPending(int chooser, Event option) {
        return engine.pendingInput().orElseThrow() instanceof AwaitingChoice(var player, var options)
                && player == chooser
                && options.contains(single(option))
                && options.contains(single(new NoOpEvent(chooser)));
    }

    private static Followup select(Character... characters) {
        val events = new java.util.ArrayList<Event>();
        for (int i = 0; i < characters.length; i++) {
            events.add(new SelectCharacterEvent(i + 1, characters[i]));
        }
        events.add(new CreateDestinyDeckEvent());
        events.add(new StackDestinyDeckEvent(DESTINY));
        return all(events.toArray(Event[]::new));
    }

    private static Followup give(Map<Integer, List<SkillCard>> cards) {
        return all(cards.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(card -> new GiveSkillCardEvent(entry.getKey(), card)))
                .toArray(Event[]::new));
    }

    private void start(int playerCount, Followup script) {
        players = playerCount;
        engine = new GameEngine(playerCount, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    private Game game() {
        return (Game) engine.view();
    }

    private void assertWaitingFor(PendingInput expected) {
        assertEquals(EngineStatus.waiting(expected), engine.status(expected.player()));
    }

    private void assertRestoresTheSameGame() {
        val restored = GameEngine.restore(engine.journal());

        assertEquals(GameSnapshot.of((Game) engine.view()), GameSnapshot.of((Game) restored.view()));
        assertEquals(statuses(engine), statuses(restored));
    }

    private List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, players).mapToObj(engine::status).toList();
    }
}
