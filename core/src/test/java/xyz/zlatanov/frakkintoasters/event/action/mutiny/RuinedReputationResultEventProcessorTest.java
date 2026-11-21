package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class RuinedReputationResultEventProcessorTest extends EventTestHarness<RuinedReputationResultEvent> {

    @BeforeEach
    void setUp() {
        player(2).character(WILLIAM_ADAMA);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
    }

    @Test
    void onFourOrLessTheTargetGoesToTheBrig() {
        rolled(4);

        execute(new RuinedReputationResultEvent(2));

        assertFollowup(new SendToBrigEvent(2));
    }

    @Test
    void higherNothingHappens() {
        rolled(5);

        execute(new RuinedReputationResultEvent(2));
    }

    @Test
    void notWhenHeIsInTheBrigAlready() {
        moveTo(BRIG, WILLIAM_ADAMA);
        rolled(1);

        execute(new RuinedReputationResultEvent(2));
    }
}
