package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ENCOURAGE_MUTINY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class EncourageMutinyResultEventProcessorTest extends EventTestHarness<EncourageMutinyResultEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        admiral(WILLIAM_ADAMA);
    }

    @Test
    void onThreeThePlayerBecomesAdmiral() {
        rolled(3);

        execute(new EncourageMutinyResultEvent(3));

        assertFollowup(new MakeAdmiralEvent(KARA_STARBUCK_THRACE));
        assertEquals(List.of(ENCOURAGE_MUTINY), quorumDeck.discardedCards());
    }

    @Test
    void lowerTheFleetLosesMorale() {
        rolled(2);

        execute(new EncourageMutinyResultEvent(3));

        assertFollowup(lose(MORALE, 1));
        assertEquals(List.of(ENCOURAGE_MUTINY), quorumDeck.discardedCards());
    }
}
