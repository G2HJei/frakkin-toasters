package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseSuperCrisisSectionEvent;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.location.cylon.CapricaPlaySuperCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.DealLoyaltyCardsEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealCylonEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.GiveSuperCrisisCardToPlayerEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsIntoSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.result.HumanPrisonerTakeHandEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.MOVE;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.STATE_OF_EMERGENCY;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_REDUCE_MORALE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.*;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.LOYALTY;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.SUPER_CRISIS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.REPAIR;

/**
 * A revealed Cylon plays Super Crisis Cards at "Caprica". Kara "Starbuck" Thrace is player 1, the current player and a
 * Cylon, who reveals and then acts again through a "State of Emergency" grant. William Adama (Admiral) is player 2, Laura
 * Roslin (President) player 3. The Destiny deck is stacked with two Engineering cards, so every check fails.
 */
class SuperCrisisCardTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private static final List<SkillCard> DESTINY = List.of(new SkillCard(1, REPAIR), new SkillCard(2, REPAIR));

    private GameEngine engine;

    // The Cylon who played the card takes the hand of the human he picks, who goes to "Sickbay".
    @Test
    void humanPrisoner() {
        val laurasHand = List.of(new SkillCard(3, REPAIR), new SkillCard(4, REPAIR));
        start(all(revealedCylonAtCaprica(HUMAN_PRISONER),
                all(laurasHand.stream().map(card -> single(new GiveSkillCardEvent(3, card))).toArray(Followup[]::new)),
                single(new GrantEvent(1, ACTION, STATE_OF_EMERGENCY)), CYLONS_WIN));

        playAtCaprica(HUMAN_PRISONER);
        failTheCheck();
        choose(1, single(new HumanPrisonerTakeHandEvent(1, 3)));

        assertTrue(game().player(1).skillCards().cards().containsAll(laurasHand));
        assertTrue(game().player(3).skillCards().isEmpty());
        assertEquals(SICKBAY, game().locate(LAURA_ROSLIN).orElseThrow());
        assertEquals(List.of(HUMAN_PRISONER), game().decks().deck(SUPER_CRISIS).discardedCards());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // A card without a skill check: the Admiral picks a section.
    @Test
    void demandPeaceManifesto() {
        start(all(revealedCylonAtCaprica(DEMAND_PEACE_MANIFESTO),
                single(new GiveSkillCardEvent(2, new SkillCard(3, REPAIR))),
                single(new GrantEvent(1, ACTION, STATE_OF_EMERGENCY)), CYLONS_WIN));

        playAtCaprica(DEMAND_PEACE_MANIFESTO);
        choose(2, single(new ChooseSuperCrisisSectionEvent(2, DEMAND_PEACE_MANIFESTO, BOTTOM)));

        assertTrue(game().player(2).skillCards().isEmpty());
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // "Bomb on Colonial One" fails: Laura Roslin goes to "Sickbay", the card stays in play and nobody moves to Colonial
    // One any more.
    @Test
    void bombOnColonialOne() {
        start(all(revealedCylonAtCaprica(BOMB_ON_COLONIAL_1),
                single(new GrantEvent(1, ACTION, STATE_OF_EMERGENCY)),
                single(new MoveCharacterEvent(WILLIAM_ADAMA, COMMAND)),
                single(new GrantEvent(2, MOVE, STATE_OF_EMERGENCY)), CYLONS_WIN));

        playAtCaprica(BOMB_ON_COLONIAL_1);
        failTheCheck();

        assertEquals(SICKBAY, game().locate(LAURA_ROSLIN).orElseThrow());
        assertTrue(game().colonialOneDestroyed());
        assertTrue(game().decks().deck(SUPER_CRISIS).discardedCards().isEmpty());

        choose(2, single(new PlayerDecisionEvent<>(2, MoveEvent.class)));
        val toColonialOne = new MoveEvent(2, PRESIDENTS_OFFICE, null);
        assertThrows(InvalidActionException.class, () -> engine.submit(2, toColonialOne));
        play(engine -> engine.submit(2, new MoveEvent(2, ARMORY, null)));
        assertEquals(new Finished(CYLONS), engine.status(1));
    }

    // Kara Thrace holds the card, reveals as a Cylon (declining the power), which ends her turn, and is moved to
    // "Caprica".
    private static Followup revealedCylonAtCaprica(SuperCrisisCard card) {
        return all(
                new SelectCharacterEvent(1, KARA_STARBUCK_THRACE),
                new SelectCharacterEvent(2, WILLIAM_ADAMA),
                new SelectCharacterEvent(3, LAURA_ROSLIN),
                new MakePresidentEvent(LAURA_ROSLIN),
                new MakeAdmiralEvent(WILLIAM_ADAMA),
                new MoveCharacterEvent(KARA_STARBUCK_THRACE, HANGAR_DECK),
                new MoveCharacterEvent(WILLIAM_ADAMA, ADMIRALS_QUARTERS),
                new MoveCharacterEvent(LAURA_ROSLIN, PRESIDENTS_OFFICE),
                new CreateDestinyDeckEvent(),
                new StackDestinyDeckEvent(DESTINY),
                new GiveSuperCrisisCardToPlayerEvent(1, card),
                new StackDeckEvent(LOYALTY, List.of(CYLON_REDUCE_MORALE)),
                new DealLoyaltyCardsEvent(1, 1, false),
                new RevealCylonEvent(1, CYLON_REDUCE_MORALE),
                new MoveCharacterEvent(KARA_STARBUCK_THRACE, CAPRICA));
    }

    private void playAtCaprica(SuperCrisisCard card) {
        choose(1, single(new NoOpEvent(1))); // she declines the power
        assertTrue(game().player(1).isRevealedCylon());
        choose(1, single(new PlayerDecisionEvent<>(1, ActionEvent.class)));
        play(engine -> engine.submit(1, new CapricaPlaySuperCrisisCardEvent(1, card)));
        assertFalse(game().player(1).superCrisisCards().cards().contains(card));
    }

    // Nobody plays a card, before or into the check, from the player to the left of the current player. Two Engineering
    // cards from the Destiny deck count against it.
    private void failTheCheck() {
        assertEquals(1, engine.view().activeSkillCheck().playedBy());
        for (val player : List.of(2, 3, 1)) {
            assertEquals(new AwaitingDecision(new PlayerDecisionEvent<>(player, PlayCardsBeforeSkillCheckEvent.class)),
                    engine.pendingInput().orElseThrow());
            play(engine -> engine.submit(player, new PlayCardsBeforeSkillCheckEvent(player)));
        }
        for (val player : List.of(2, 3, 1)) {
            assertEquals(new AwaitingDecision(new PlayerDecisionEvent<>(player, PlayCardsIntoSkillCheckEvent.class)),
                    engine.pendingInput().orElseThrow());
            play(engine -> engine.submit(player, new PlayCardsIntoSkillCheckEvent(player, List.of())));
        }
        // Whoever holds a card for the moment after the total, e.g. "Declare Emergency", declines it.
        while (engine.view().activeSkillCheck() != null
                && engine.pendingInput().orElseThrow() instanceof AwaitingChoice choice
                && choice.options().contains(single(new NoOpEvent(choice.player())))) {
            play(engine -> engine.choose(choice.player(), single(new NoOpEvent(choice.player()))));
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
        val choice = (AwaitingChoice) engine.pendingInput().orElseThrow();
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
