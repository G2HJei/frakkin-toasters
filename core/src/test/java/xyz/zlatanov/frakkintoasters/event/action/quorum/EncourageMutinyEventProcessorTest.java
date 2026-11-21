package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ENCOURAGE_MUTINY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class EncourageMutinyEventProcessorTest extends EventTestHarness<EncourageMutinyEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        presidentHand.addOnTop(ENCOURAGE_MUTINY);
    }

    @Test
    void anotherPlayerRollsForTheTitle() {
        execute(new EncourageMutinyEvent(1, 3));

        assertFollowup(rollDie(new EncourageMutinyResultEvent(3)));
    }

    @Test
    void notTheAdmiral() {
        assertInvalid(new EncourageMutinyEvent(1, 2));
    }

    @Test
    void notThePresidentHimself() {
        assertInvalid(new EncourageMutinyEvent(1, 1));
    }
}
