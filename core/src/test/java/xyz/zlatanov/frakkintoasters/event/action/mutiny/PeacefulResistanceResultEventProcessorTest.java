package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class PeacefulResistanceResultEventProcessorTest extends EventTestHarness<PeacefulResistanceResultEvent> {

    @BeforeEach
    void setUp() {
        player(2).character(WILLIAM_ADAMA);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
    }

    @Test
    void onFourOrLessTheAdmiralGoesToTheBrig() {
        rolled(4);

        execute(new PeacefulResistanceResultEvent());

        assertFollowup(new SendToBrigEvent(2));
    }

    @Test
    void higherNothingHappens() {
        rolled(5);

        execute(new PeacefulResistanceResultEvent());
    }
}
