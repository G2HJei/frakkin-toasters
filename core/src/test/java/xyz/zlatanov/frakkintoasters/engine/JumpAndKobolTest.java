package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.JumpingTheFleetEvent;
import xyz.zlatanov.frakkintoasters.event.jump.ChooseDestinationEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceInSetupLocationEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PEGASUS_CIC;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.DESTINATION;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.LOYALTY;

/**
 * The jumps of a seeded game up to the Kobol end game, through {@code submit} and {@code choose} alone, restoring the
 * game at every pause: the Admiral's choice of the destination, the Sleeper Agent phase at 4 distance, and the final jump
 * after 8. The Destination deck is stacked so that each jump offers known cards: 3, 2, 2 and 1 distance.
 */
class JumpAndKobolTest {

    private static final int ADMIRAL = 1;

    // Helena Cain is the Admiral, Laura Roslin the President and Kara Thrace the CAG. Player 1 is the current player.
    private static Followup setUp(List<LoyaltyCard> sleeperAgentCards) {
        return all(
                all(new SelectCharacterEvent(1, HELENA_CAIN),
                        new SelectCharacterEvent(2, LAURA_ROSLIN),
                        new SelectCharacterEvent(3, KARA_STARBUCK_THRACE),
                        new DetermineNextPresidentEvent(),
                        new DetermineNextAdmiralEvent(),
                        new DetermineNextCagEvent()),
                single(new SetUpGalacticaEvent()),
                // each jump draws two cards and the one not chosen goes to the bottom
                single(new StackDeckEvent(DESTINATION, List.of(
                        DESOLATE_MOON, GAS_GIANT, BARREN_PLANET, ALGAE_PLANET, DEEP_SPACE, DYING_STAR, ALGAE_PLANET, GAS_GIANT))),
                single(new StackDeckEvent(LOYALTY, sleeperAgentCards)));
    }

    private GameEngine engine;

    @Test
    void theHumansWinTheKobolEndGame() {
        start(all(setUp(List.of(NOT_CYLON, NOT_CYLON, NOT_CYLON)), jumps(5)));

        // 1. The fleet jumps: vipers and Cylon ships leave the space areas, and the Admiral chooses a destination.
        assertWaitingFor(destinationChoice(DESOLATE_MOON, GAS_GIANT));
        assertEquals(0, engine.view().boards().galactica().shipsInSpace(Viper.class).size());
        assertEquals(0, engine.view().boards().galactica().shipsInSpace(Raider.class).size());
        assertEquals(0, engine.view().boards().galactica().shipsInSpace(Basestar.class).size());
        assertEquals(4, engine.view().boards().cylonFleet().shipsInSpace(xyz.zlatanov.frakkintoasters.state.ship.CylonShip.class).size()); // a basestar and 3 raiders
        play(engine -> engine.choose(ADMIRAL, destination(DESOLATE_MOON, GAS_GIANT)));
        assertEquals(3, engine.view().distanceTraveled());
        assertEquals(5, engine.view().boards().galactica().fuel());

        // 2. 5 distance: the Sleeper Agent phase deals a Loyalty Card to each player.
        assertWaitingFor(destinationChoice(BARREN_PLANET, ALGAE_PLANET));
        assertEquals(List.of(0, 0, 0), loyaltyCards());
        play(engine -> engine.choose(ADMIRAL, destination(BARREN_PLANET, ALGAE_PLANET)));
        assertEquals(5, engine.view().distanceTraveled());
        assertEquals(List.of(1, 1, 1), loyaltyCards());

        // 3. and 4.
        assertWaitingFor(destinationChoice(DEEP_SPACE, DYING_STAR));
        play(engine -> engine.choose(ADMIRAL, destination(DEEP_SPACE, DYING_STAR)));
        assertWaitingFor(destinationChoice(ALGAE_PLANET, GAS_GIANT));
        play(engine -> engine.choose(ADMIRAL, destination(ALGAE_PLANET, GAS_GIANT)));

        assertEquals(8, engine.view().distanceTraveled());
        assertEquals(List.of(DESOLATE_MOON, BARREN_PLANET, DEEP_SPACE, ALGAE_PLANET), engine.view().destinations());
        assertEquals(1, engine.view().boards().galactica().fuel());
        assertEquals(JumpPreparation.START, engine.view().boards().galactica().jumpPreparation());

        // 5. "After eight or more units of distance, the humans are only one jump away from winning the game."
        assertEquals(new EngineStatus.Finished(Team.HUMANS), engine.status(1));
        assertEquals(List.of(1, 2, 3), engine.view().winners());
        assertTrue(engine.view().boards().galactica().fuel() > 0);
    }

    // "Each unrevealed Personal Goal Loyalty Card ... reduces the resource listed on the card. If any resource has been
    // reduced to 0, then the victory goes to the Cylons."
    @Test
    void aPersonalGoalCanTurnTheEndGameIntoACylonVictory() {
        // "Selfish" costs 1 fuel, and the fleet has 1 left.
        start(all(setUp(List.of(NOT_CYLON, NOT_CYLON_SELFISH, NOT_CYLON)), jumps(5)));

        for (val pair : List.of(List.of(DESOLATE_MOON, GAS_GIANT), List.of(BARREN_PLANET, ALGAE_PLANET),
                List.of(DEEP_SPACE, DYING_STAR), List.of(ALGAE_PLANET, GAS_GIANT))) {
            play(engine -> engine.choose(ADMIRAL, destination(pair.get(0), pair.get(1))));
        }

        assertEquals(0, engine.view().boards().galactica().fuel());
        assertEquals(new EngineStatus.Finished(Team.CYLONS), engine.status(1));
        assertEquals(List.of(), engine.view().winners());
    }

    /* inputs */

    private static Followup jumps(int count) {
        return all(java.util.Collections.nCopies(count, single(new JumpingTheFleetEvent())).toArray(Followup[]::new));
    }

    private static Followup destination(DestinationCard chosen, DestinationCard other) {
        return single(new ChooseDestinationEvent(ADMIRAL, chosen, other));
    }

    private static AwaitingChoice destinationChoice(DestinationCard first, DestinationCard second) {
        return new AwaitingChoice(ADMIRAL, Set.of(destination(first, second), destination(second, first)));
    }

    private void start(Followup script) {
        engine = new GameEngine(3, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
        // Helena Cain chooses where she starts.
        play(engine -> engine.choose(1, single(new PlaceInSetupLocationEvent(1, PEGASUS_CIC))));
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    private List<Integer> loyaltyCards() {
        return engine.view().players().stream().map(player -> player.loyaltyCards().size()).toList();
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

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList();
    }
}
