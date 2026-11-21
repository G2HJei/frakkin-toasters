package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.RELEASE_CYLON_MUGSHOTS;

class ReleaseCylonMugshotsResultEventProcessorTest extends EventTestHarness<ReleaseCylonMugshotsResultEvent> {

    @Test
    void onThreeTheFleetLosesMorale() {
        rolled(3);

        execute(new ReleaseCylonMugshotsResultEvent());

        assertFollowup(lose(MORALE, 1));
        assertEquals(List.of(RELEASE_CYLON_MUGSHOTS), quorumDeck.discardedCards());
    }

    @Test
    void higherNothingHappens() {
        rolled(4);

        execute(new ReleaseCylonMugshotsResultEvent());

        assertEquals(List.of(RELEASE_CYLON_MUGSHOTS), quorumDeck.discardedCards());
    }
}
