package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.AUTHORIZATION_OF_BRUTAL_FORCE;

class AuthorizationOfBrutalForceResultEventProcessorTest extends EventTestHarness<AuthorizationOfBrutalForceResultEvent> {

    @Test
    void onTwoTheFleetLosesPopulation() {
        rolled(2);

        execute(new AuthorizationOfBrutalForceResultEvent());

        assertFollowup(lose(POPULATION, 1));
        assertEquals(List.of(AUTHORIZATION_OF_BRUTAL_FORCE), quorumDeck.discardedCards());
    }

    @Test
    void higherNothingHappens() {
        rolled(3);

        execute(new AuthorizationOfBrutalForceResultEvent());

        assertEquals(List.of(AUTHORIZATION_OF_BRUTAL_FORCE), quorumDeck.discardedCards());
    }
}
