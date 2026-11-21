package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMINISTRATION;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.ROMO_LAMPKIN;

class KleptomaniaBrigEventProcessorTest extends EventTestHarness<KleptomaniaBrigEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(ROMO_LAMPKIN);
    }

    @Test
    void sendsHimToTheBrig() {
        moveTo(ADMINISTRATION, ROMO_LAMPKIN);

        execute(new KleptomaniaBrigEvent(1));

        assertFollowup(new SendToBrigEvent(1));
    }

    // Something sent him there during the turn already.
    @Test
    void nothingWhenHeIsInTheBrigAlready() {
        moveTo(BRIG, ROMO_LAMPKIN);

        execute(new KleptomaniaBrigEvent(1));
    }
}
