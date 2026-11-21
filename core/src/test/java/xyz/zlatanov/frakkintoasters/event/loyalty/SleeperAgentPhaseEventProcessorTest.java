package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.character.SleeperAgentEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class SleeperAgentPhaseEventProcessorTest extends EventTestHarness<SleeperAgentPhaseEvent> {

    @BeforeEach
    void players() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
    }

    // "Each player is dealt one card from the top of the Loyalty deck." (rule 5.5)
    @Test
    void everyoneIsDealtOneCardFromTheCurrentPlayerOn() {
        execute(new SleeperAgentPhaseEvent());

        assertFollowup(all(
                single(new DealLoyaltyCardsEvent(1, 1, false)),
                single(new DealLoyaltyCardsEvent(2, 1, false)),
                single(new DealLoyaltyCardsEvent(3, 1, false)),
                single(new FinishSleeperAgentPhaseEvent())));
    }

    @Test
    void theDealStartsWithTheCurrentPlayer() {
        ops.changeCurrentPlayer(2);

        execute(new SleeperAgentPhaseEvent());

        assertFollowup(all(
                single(new DealLoyaltyCardsEvent(2, 1, false)),
                single(new DealLoyaltyCardsEvent(3, 1, false)),
                single(new DealLoyaltyCardsEvent(1, 1, false)),
                single(new FinishSleeperAgentPhaseEvent())));
    }

    // Boomer's "Sleeper Agent": 2 cards, then the "Brig". Baltar's "Coward" card comes at the start of the game only.
    @Test
    void boomerIsDealtTwoThenGoesToTheBrigAndBaltarOne() {
        setUpGame(3);
        player(1).character(SHARON_BOOMER_VALERII);
        player(2).character(GAIUS_BALTAR);
        player(3).character(LAURA_ROSLIN);

        execute(new SleeperAgentPhaseEvent());

        assertFollowup(all(
                all(new DealLoyaltyCardsEvent(1, 2, false), new SleeperAgentEvent(1)),
                single(new DealLoyaltyCardsEvent(2, 1, false)),
                single(new DealLoyaltyCardsEvent(3, 1, false)),
                single(new FinishSleeperAgentPhaseEvent())));
    }

    // "She is given only one Loyalty Card during the Sleeper Agent phase, rather than the two" (rule 6.1.2.2)
    @Test
    void aRevealedBoomerIsDealtOne() {
        setUpGame(3);
        player(1).character(SHARON_BOOMER_VALERII);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        revealCylon();

        execute(new SleeperAgentPhaseEvent());

        assertFollowup(all(
                single(new DealLoyaltyCardsEvent(1, 1, false)),
                single(new DealLoyaltyCardsEvent(2, 1, false)),
                single(new DealLoyaltyCardsEvent(3, 1, false)),
                single(new FinishSleeperAgentPhaseEvent())));
    }

    // "Cylon Leaders get two additional Motive cards." They get no Loyalty Cards.
    @Test
    void aCylonLeaderIsDealtMotiveCardsOnly() {
        setUpGame(4);
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        player(4).character(SHARON_ATHENA_AGATHON);

        execute(new SleeperAgentPhaseEvent());

        assertEquals(2, player(4).motiveCards().size());
        assertFollowup(all(
                single(new DealLoyaltyCardsEvent(1, 1, false)),
                single(new DealLoyaltyCardsEvent(2, 1, false)),
                single(new DealLoyaltyCardsEvent(3, 1, false)),
                single(new FinishSleeperAgentPhaseEvent())));
    }
}
