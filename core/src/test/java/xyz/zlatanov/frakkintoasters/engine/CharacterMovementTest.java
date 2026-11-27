package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.MovementAbilityEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.AggressiveTacticsEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.GlimpseTheFaceOfGodEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.GlimpseTheFaceOfGodPlaceEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.IntimateEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.PrimacyEvent;
import xyz.zlatanov.frakkintoasters.event.character.PoliticallyAdroitEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceInSetupLocationEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.GiveSuperCrisisCardToPlayerEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.turn.DiscardStepEvent;
import xyz.zlatanov.frakkintoasters.event.turn.MovementStepEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.INBOUND_NUKES;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;

/**
 * Player 1 has the ability.
 */
class CharacterMovementTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private static final SkillCard ORDER = new SkillCard(2, EXECUTIVE_ORDER);
    private static final SkillCard SCOUT = new SkillCard(2, LAUNCH_SCOUT);

    private GameEngine engine;

    // The other player draws from the deck she names.
    @Test
    void intimateTakesACardAndTheOtherDraws() {
        start(all(setUp(CAPRICA_SIX), single(new GiveSkillCardEvent(2, ORDER)), single(new MovementStepEvent()),
                CYLONS_WIN));

        useMovementAbility(1);
        play(engine -> engine.submit(1, new IntimateEvent(1, 2, TREACHERY)));

        assertEquals(List.of(ORDER), game().player(1).skillCards().cards());
        val drawn = game().player(2).skillCards().cards();
        assertEquals(1, drawn.size());
        assertEquals(TREACHERY, drawn.getFirst().type().color());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // Two actions in his Movement step, for his miracle token.
    @Test
    void aggressiveTacticsTakesTwoActions() {
        start(all(setUp(CAVIL), single(new MovementStepEvent()), CYLONS_WIN));

        useMovementAbility(1);
        play(engine -> engine.submit(1, new AggressiveTacticsEvent(1)));
        act(1);
        play(engine -> engine.submit(1, new PrimacyEvent(1, null, false)));
        act(1);
        play(engine -> engine.submit(1, new PrimacyEvent(1, null, false)));

        assertEquals(2, galacticaBasestars());
        assertEquals(0, game().player(1).miracleTokens());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // He keeps neither: both go back on top, the one he names first on top.
    @Test
    void glimpseTheFaceOfGodTakesTwoDestinyCardsAndPutsTwoBack() {
        start(all(setUp(LEOBEN_CONOY), single(new StackDestinyDeckEvent(List.of(ORDER, SCOUT))),
                single(new MovementStepEvent()), CYLONS_WIN));

        useMovementAbility(1);
        play(engine -> engine.submit(1, new GlimpseTheFaceOfGodEvent(1)));
        assertEquals(Set.of(ORDER, SCOUT), Set.copyOf(game().player(1).skillCards().cards()));
        play(engine -> engine.submit(1, new GlimpseTheFaceOfGodPlaceEvent(1, List.of(SCOUT, ORDER))));

        assertEquals(List.of(SCOUT, ORDER), game().decks().destiny().cards().subList(0, 2));
        assertEquals(0, game().player(1).skillCards().size());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // Without 2 cards to discard, he goes to the "Brig" when his turn ends.
    @Test
    void kleptomaniaSendsRomoToTheBrigAtTheEndOfHisTurn() {
        start(all(setUp(ROMO_LAMPKIN), single(new MoveCharacterEvent(WILLIAM_ADAMA, ADMINISTRATION)),
                single(new MovementStepEvent()), single(new DiscardStepEvent()), CYLONS_WIN));

        choose(1, single(new NoOpEvent(1)));

        assertEquals(BRIG, game().locate(ROMO_LAMPKIN).orElseThrow());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // A card for William Adama, then 2 from her skill set.
    @Test
    void politicallyAdroitGivesACardForTwo() {
        start(all(setUp(ELLEN_TIGH), single(new GiveSkillCardEvent(1, ORDER)), single(new MovementStepEvent()),
                CYLONS_WIN));

        choose(1, single(new NoOpEvent(1)));
        choose(1, single(new PlayerDecisionEvent<>(1, PoliticallyAdroitEvent.class)));
        play(engine -> engine.submit(1, new PoliticallyAdroitEvent(1, 2, ORDER)));
        play(engine -> engine.submit(1, new DrawSkillCardsEvent(1, Map.of(POLITICS, 2))));

        assertEquals(List.of(ORDER), game().player(2).skillCards().cards());
        assertEquals(2, game().player(1).skillCards().size());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // With one card she pays it at once.
    @Test
    void hereticCostsDannaASuperCrisisCardToLeaveTheResurrectionShip() {
        start(all(setUp(DANNA_BIERS), single(new MoveCharacterEvent(DANNA_BIERS, RESURRECTION_SHIP)),
                single(new GiveSuperCrisisCardToPlayerEvent(1, INBOUND_NUKES)), single(new MovementStepEvent()),
                CYLONS_WIN), () -> choose(1, single(new PlaceInSetupLocationEvent(1, HUMAN_FLEET))));

        choose(1, single(new PlayerDecisionEvent<>(1, MoveEvent.class)));
        play(engine -> engine.submit(1, new MoveEvent(1, CAPRICA, null)));

        assertEquals(CAPRICA, game().locate(DANNA_BIERS).orElseThrow());
        assertTrue(game().player(1).superCrisisCards().isEmpty());
        assertEquals(List.of(INBOUND_NUKES), game().decks().superCrisis().discardedCards());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // Four players, so a Cylon Leader may be chosen, of the types the setup allows: the first one, a Military Leader, a
    // Pilot and a Support character.
    private static Followup setUp(Character first) {
        return all(
                new SelectCharacterEvent(1, first),
                new SelectCharacterEvent(2, WILLIAM_ADAMA),
                new SelectCharacterEvent(3, KARA_STARBUCK_THRACE),
                new SelectCharacterEvent(4, CHIEF_GALEN_TYROL));
    }

    private void start(Followup script) {
        start(script, () -> {
        });
    }

    // Some characters choose where they start, which the script can't answer.
    private void start(Followup script, Runnable setupChoices) {
        engine = new GameEngine(4, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
        setupChoices.run();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    private void useMovementAbility(int player) {
        choose(player, single(new PlayerDecisionEvent<>(player, MovementAbilityEvent.class)));
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

    private int galacticaBasestars() {
        return engine.view().boards().galactica().shipsInSpace(Basestar.class).size();
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
        return IntStream.rangeClosed(1, 4).mapToObj(engine::status).toList();
    }
}
