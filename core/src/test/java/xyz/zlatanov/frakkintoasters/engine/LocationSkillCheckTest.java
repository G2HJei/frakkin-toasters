package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.AdmiralsQuartersEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.BrigEvent;
import xyz.zlatanov.frakkintoasters.event.location.pegasus.AirlockEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.CreateLoyaltyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.*;
import xyz.zlatanov.frakkintoasters.event.skillcheck.*;
import xyz.zlatanov.frakkintoasters.event.turn.StartTurnEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DESTINATION_GALACTICA;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RIOTS_NO_JUMP;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_5;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;

/**
 * The skill checks that game board locations start (rule 10.3.3), each run from the action to the end of the check
 * through {@code submit} and {@code choose} alone, restoring the game at every pause.
 */
class LocationSkillCheckTest {

    // Only the end of the game empties the agenda, so the script ends it after the action.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private GameEngine engine;

    // The top of the Destiny deck: 2 cards for the check, and 2 more for "Trust Instincts". None has an ability.
    private static final List<SkillCard> DESTINY = List.of(
            new SkillCard(1, CONSOLIDATE_POWER), new SkillCard(1, LAUNCH_SCOUT),
            new SkillCard(2, CONSOLIDATE_POWER), new SkillCard(1, LAUNCH_SCOUT));

    // "Action: Choose a character, then pass this skill check to send him to the "Brig.""
    @Test
    void admiralsQuarters() {
        val cards = Map.of(
                1, List.of(new SkillCard(3, EXECUTIVE_ORDER)),
                2, List.of(new SkillCard(2, EXECUTIVE_ORDER)),
                3, List.of(new SkillCard(3, LAUNCH_SCOUT)));
        start(all(
                setUp(),
                give(cards),
                single(new PlayerDecisionEvent<>(1, ActionEvent.class)),
                CYLONS_WIN));

        play(engine -> engine.submit(1, new AdmiralsQuartersEvent(1, 3)));
        assertEquals(SkillCheckType.ADMIRALS_QUARTERS, engine.view().activeSkillCheck().type());

        passPoliticalProwess();
        playNothingBefore();
        playInto(cards);
        // Leadership and tactics count: 3 + 2 + 3, and the Destiny cards -1 + 1.
        finishTheCheck(8);

        assertEquals(Optional.of(BRIG), engine.view().locate(KARA_STARBUCK_THRACE));
        assertSkillCheckEnded();
    }

    // "Action: Pass this skill check to move to any location", on Galactica (rule 9.2.4). In the "Brig" the character
    // adds only 1 card.
    @Test
    void brig() {
        val cards = Map.of(
                1, List.of(new SkillCard(1, RED_TAPE)), // 1 card only, from the "Brig"
                2, List.of(new SkillCard(3, CONSOLIDATE_POWER), new SkillCard(1, TRUST_INSTINCTS)),
                3, List.of(new SkillCard(5, LAUNCH_SCOUT)));
        start(all(
                setUp(),
                give(cards),
                single(new SendToBrigEvent(1)),
                single(new PlayerDecisionEvent<>(1, ActionEvent.class)),
                CYLONS_WIN));

        play(engine -> engine.submit(1, new BrigEvent(1)));
        assertEquals(1, engine.view().activeSkillCheck().targetPlayerNumber());

        passPoliticalProwess();
        playNothingBefore();
        playInto(cards);
        resolveRedTapeFirst();
        // Politics and tactics count: 1 + 3 + 1, the 5 that "Red Tape" discards doesn't, the Destiny cards 1 + 1, and the
        // 2 that "Trust Instincts" adds 2 + 1.
        finishTheCheck(10);

        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(1, LeaveBrigEvent.class, DESTINATION_GALACTICA)));
        play(engine -> engine.submit(1, new LeaveBrigEvent(1, COMMAND)));

        assertEquals(Optional.of(COMMAND), engine.view().locate(WILLIAM_ADAMA));
        assertSkillCheckEnded();
    }

    // "Action: Choose a character and pass this skill check to execute him." The executed human's player chooses a new
    // character, of any type now that the game is under way, and gets a new Loyalty Card (rule 7.1). The action is
    // taken in a turn, which goes on to the next player's: after the Crisis step, "Riots", that the Admiral resolves.
    @Test
    void airlock() {
        // Helena Cain gets 1 card before the game and plays only that one: the 5 she draws in her turn stay in her hand.
        val cards = Map.of(
                1, List.of(new SkillCard(2, RED_TAPE)),
                2, List.of(new SkillCard(4, CONSOLIDATE_POWER), new SkillCard(1, TRUST_INSTINCTS)),
                3, List.of(new SkillCard(3, LAUNCH_SCOUT), new SkillCard(5, LAUNCH_SCOUT)));
        start(all(
                all(new SelectCharacterEvent(1, HELENA_CAIN),
                        new SelectCharacterEvent(2, LAURA_ROSLIN),
                        new SelectCharacterEvent(3, KARA_STARBUCK_THRACE),
                        new DetermineNextPresidentEvent(),
                        new DetermineNextAdmiralEvent(),
                        new DetermineNextCagEvent(),
                        new SetUpGalacticaEvent(),
                        new StackCrisisCardEvent(RIOTS_NO_JUMP),
                        new CreateDestinyDeckEvent(),
                        new StackDestinyDeckEvent(DESTINY),
                        new CreateLoyaltyDeckEvent()),
                give(cards),
                single(new StartTurnEvent())));
        play(engine -> engine.choose(1, single(new PlaceInSetupLocationEvent(1, PEGASUS_CIC))));
        // The seed deals the Cylon card to one of the players, so the target is a human.
        val target = IntStream.of(2, 3)
                .filter(p -> engine.view().player(p).loyaltyCards().cards().stream().noneMatch(LoyaltyCard::isCylon))
                .findFirst()
                .orElseThrow();
        val targetCharacter = engine.view().player(target).character();

        // Helena Cain's turn: she draws, moves to the "Airlock" on Pegasus and uses it.
        play(engine -> engine.submit(1, new DrawSkillCardsEvent(1, Map.of(LEADERSHIP, 2, TACTICS, 3))));
        play(engine -> engine.choose(1, single(new PlayerDecisionEvent<>(1, MoveEvent.class))));
        play(engine -> engine.submit(1, new MoveEvent(1, AIRLOCK, null)));
        play(engine -> engine.choose(1, single(new PlayerDecisionEvent<>(1, ActionEvent.class))));
        play(engine -> engine.submit(1, new AirlockEvent(1, target)));

        passPoliticalProwess();
        playNothingBefore();
        playInto(cards);
        resolveRedTapeFirst();
        val morale = engine.view().boards().galactica().morale();
        // Politics, tactics and treachery count: 2 + 4 + 1 + 3, the 5 that "Red Tape" discards doesn't, the Destiny cards
        // 1 + 1, and the 2 that "Trust Instincts" adds 2 + 1.
        finishTheCheck(15);

        // "Lose 1 morale", and the character leaves the game.
        assertEquals(morale - 1, engine.view().boards().galactica().morale());
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(target, SelectCharacterEvent.class)));
        assertNull(engine.view().player(target).character());
        assertTrue(engine.view().removedComponents().contains(targetCharacter));
        play(engine -> engine.submit(target, new SelectCharacterEvent(target, SAUL_TIGH)));

        assertEquals(Optional.of(COMMAND), engine.view().locate(SAUL_TIGH));
        assertEquals(1, engine.view().player(target).loyaltyCards().size());
        assertNull(engine.view().activeSkillCheck());
        assertTrue(scopesOpened().stream().anyMatch(event -> event instanceof EndSkillCheckEvent));
        // Helena Cain's turn goes on: she is the Admiral, so she chooses the top of the Crisis Card, which costs 1 food and
        // 1 morale on top of the execution's.
        assertWaitingFor(new AwaitingChoice(1, Set.of(single(new ChooseCrisisSectionEvent(1, RIOTS_NO_JUMP, TOP)),
                single(new ChooseCrisisSectionEvent(1, RIOTS_NO_JUMP, BOTTOM)))));
        play(engine -> engine.choose(1, single(new ChooseCrisisSectionEvent(1, RIOTS_NO_JUMP, TOP))));
        assertEquals(7, engine.view().boards().galactica().food());
        assertEquals(morale - 2, engine.view().boards().galactica().morale());
        // Her turn went on to its end, and Laura Roslin's begins.
        assertEquals(2, engine.view().turn().currentPlayerNumber());
        assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(2, DrawSkillCardsEvent.class, DRAW_EXACTLY_5)));
    }

    /* the skill check, step by step */

    // Before a skill check triggered by a location, each player may play "Political Prowess".
    private void passPoliticalProwess() {
        for (int player = 1; player <= 3; player++) {
            val p = player;
            assertWaitingFor(new AwaitingChoice(p, Set.of(
                    single(new PlayerDecisionEvent<>(p, PoliticalProwessEvent.class)), single(new NoOpEvent(p)))));
            play(engine -> engine.choose(p, single(new NoOpEvent(p))));
        }
    }

    // From the player to the left of the current player, ending with the current player.
    private void playNothingBefore() {
        for (val player : List.of(2, 3, 1)) {
            assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(player, PlayCardsBeforeSkillCheckEvent.class)));
            play(engine -> engine.submit(player, new PlayCardsBeforeSkillCheckEvent(player)));
        }
    }

    // Each player adds the cards the test gave him, from the player to the left of the current player (rule 10.3.1).
    private void playInto(Map<Integer, List<SkillCard>> cards) {
        for (val player : List.of(2, 3, 1)) {
            assertWaitingFor(new AwaitingDecision(new PlayerDecisionEvent<>(player, PlayCardsIntoSkillCheckEvent.class)));
            play(engine -> engine.submit(player, new PlayCardsIntoSkillCheckEvent(player, cards.get(player))));
        }
    }

    // The current player resolves the abilities in the order of his choosing (rule 10.4.2). "Red Tape" goes first, so it
    // discards before "Trust Instincts" adds its cards, and "Trust Instincts" is then the only one left.
    private void resolveRedTapeFirst() {
        assertWaitingFor(new AwaitingChoice(1, Set.of(
                single(new ResolveSkillCheckAbilityEvent(RED_TAPE)), single(new ResolveSkillCheckAbilityEvent(TRUST_INSTINCTS)))));
        play(engine -> engine.choose(1, single(new ResolveSkillCheckAbilityEvent(RED_TAPE))));
    }

    // The strength reaches the difficulty, so nobody is asked to "Declare Emergency", and nobody plays "Change of
    // Plans" on the pass.
    private void finishTheCheck(int totalStrength) {
        assertEquals(totalStrength, engine.view().activeSkillCheck().totalStrength());
        assertEquals(PASS, engine.view().activeSkillCheck().result());
        for (int player = 1; player <= 3; player++) {
            val p = player;
            assertWaitingFor(new AwaitingChoice(p, Set.of(
                    single(new PlayerDecisionEvent<>(p, ChangeOfPlansEvent.class)), single(new NoOpEvent(p)))));
            play(engine -> engine.choose(p, single(new NoOpEvent(p))));
        }
    }

    /* the game */

    // William Adama at the "Admiral's Quarters", Laura Roslin at the "President's Office", Kara Thrace on the "Hangar
    // Deck". Player 1 is the current player.
    private static Followup setUp() {
        return all(
                new SelectCharacterEvent(1, WILLIAM_ADAMA),
                new SelectCharacterEvent(2, LAURA_ROSLIN),
                new SelectCharacterEvent(3, KARA_STARBUCK_THRACE),
                new CreateDestinyDeckEvent(),
                new StackDestinyDeckEvent(DESTINY));
    }

    // The cards a check is decided by are dealt, not drawn, so the seed only decides what the checks don't depend on.
    private static Followup give(Map<Integer, List<SkillCard>> cards) {
        return all(IntStream.rangeClosed(1, 3).boxed()
                .flatMap(player -> cards.get(player).stream().map(card -> new GiveSkillCardEvent(player, card)))
                .toArray(Event[]::new));
    }

    private void start(Followup script) {
        engine = new GameEngine(3, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    /* assertions */

    private void assertSkillCheckEnded() {
        assertNull(engine.view().activeSkillCheck());
        assertTrue(scopesOpened().stream().anyMatch(event -> event instanceof EndSkillCheckEvent));
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    private void assertWaitingFor(PendingInput expected) {
        assertEquals(EngineStatus.waiting(expected), engine.status(expected.player()));
    }

    private void assertRestoresTheSameGame() {
        val restored = GameEngine.restore(engine.journal());

        assertEquals(snapshot(engine), snapshot(restored));
        assertEquals(statuses(engine), statuses(restored));
    }

    private List<Event> scopesOpened() {
        return engine.journal().entries().stream()
                .filter(ScopeOpened.class::isInstance)
                .map(entry -> ((ScopeOpened) entry).event())
                .toList();
    }

    // The view is the game itself.
    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
