package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterBasestarOrCenturionDestroyed;

import static org.junit.jupiter.api.Assertions.assertNull;

class DestroyCenturionEventProcessorTest extends EventTestHarness<DestroyCenturionEvent> {

    // "Major Victory" may be played after a centurion is destroyed.
    @Test
    void theCenturionIsDestroyed() {
        val centurion = centurion();
        galacticaBoard.boardGalactica(centurion);

        execute(new DestroyCenturionEvent(centurion.id()));

        assertNull(galacticaBoard.boardingPartyTrack().get(centurion));
        assertFollowup(new OpenInterruptWindowEvent(new AfterBasestarOrCenturionDestroyed()));
    }

    @Test
    void notACenturionOffTheTrack() {
        assertInvalid(new DestroyCenturionEvent(centurion().id()));
    }
}
