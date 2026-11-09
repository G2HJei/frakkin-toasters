package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.CurrentPlayerChanged;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.GameStarted;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.CreateLoyaltyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class StartGameEventProcessorTest extends EventTestHarness<StartGameEvent> {

    private static final Map<Integer, List<Integer>> CLOCKWISE_FROM = Map.of(
            1, List.of(1, 2, 3),
            2, List.of(2, 3, 1),
            3, List.of(3, 1, 2));

    private int firstPlayer;

    @BeforeEach
    void startTheGame() {
        execute(new StartGameEvent());
        firstPlayer = game.turn().currentPlayerNumber();
    }

    @Test
    void theJournalStartsWithTheGame() {
        assertInstanceOf(GameStarted.class, journal.facts().getFirst());
        assertSetupQueued();
    }

    // "Randomly choose a player to be the first player, and give him the current player token." (rule A3.7.1)
    @Test
    void aRandomFirstPlayerGetsTheCurrentPlayerToken() {
        assertTrue(CLOCKWISE_FROM.containsKey(firstPlayer));
        assertTrue(journal.facts().contains(new CurrentPlayerChanged(firstPlayer)));
        assertSetupQueued();
    }

    // Rule A3.4: two vipers below Galactica, two civilian ships behind it, a basestar and three raiders in front of it.
    @Test
    void theShipsAreSetUp() {
        assertShipCount(GALACTICA_SPACE_4_OCLOCK, Viper.class, 1);
        assertShipCount(GALACTICA_SPACE_6_OCLOCK, Viper.class, 1);
        assertShipCount(GALACTICA_SPACE_2_OCLOCK, CivilianShip.class, 2);
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Basestar.class, 1);
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 3);
        assertSetupQueued();
    }

    // Characters are chosen clockwise from the first player (rule A3.7.2), the titles go by the lines of succession
    // (rule A3.7.3), and the first player draws no Skill Cards before their turn (rule A3.9).
    @Test
    void theRestOfTheSetupIsQueuedInTheOrderOfTheRules() {
        assertSetupQueued();
    }

    private void assertSetupQueued() {
        assertFollowup(all(
                single(new CreateDestinyDeckEvent()),
                selectCharacters(CLOCKWISE_FROM.get(firstPlayer)),
                all(new DetermineNextPresidentEvent(), new DetermineNextAdmiralEvent(), new DetermineNextCagEvent()),
                single(new CreateLoyaltyDeckEvent()),
                single(new ReceiveStartingSkillsEvent()),
                single(new StartTurnEvent())));
    }

    private static Followup selectCharacters(List<Integer> order) {
        return all(order.stream()
                .map(player -> new PlayerDecisionEvent<>(player, SelectCharacterEvent.class))
                .toArray(Event[]::new));
    }
}
