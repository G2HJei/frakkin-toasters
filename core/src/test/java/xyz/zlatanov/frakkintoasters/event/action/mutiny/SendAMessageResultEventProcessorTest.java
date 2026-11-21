package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCenturionEvent;

class SendAMessageResultEventProcessorTest extends EventTestHarness<SendAMessageResultEvent> {

    @Test
    void twoMoreDestroyTheCenturionOnFive() {
        ops.galactica().placeCenturion();
        val centurion = galacticaBoard.boardingPartyTrack().keySet().iterator().next();
        rolled(5);

        execute(new SendAMessageResultEvent(centurion.id()));

        assertFollowup(new DestroyCenturionEvent(centurion.id()));
    }

    @Test
    void onFourItMisses() {
        ops.galactica().placeCenturion();
        val centurion = galacticaBoard.boardingPartyTrack().keySet().iterator().next();
        rolled(4);

        execute(new SendAMessageResultEvent(centurion.id()));
    }
}
