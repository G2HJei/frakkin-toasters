package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterBasestarOrCenturionDestroyed;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class DestroyCenturionEventProcessor extends EventProcessor<DestroyCenturionEvent> {

    @Override
    protected boolean isValid() {
        return galacticaBoard.boardingPartyTrack().keySet().stream().anyMatch(c -> c.id() == event.centurionId());
    }

    @Override
    public Followup process() {
        ops.galactica().destroyCenturion(event.centurionId());
        return single(new OpenInterruptWindowEvent(new AfterBasestarOrCenturionDestroyed()));
    }
}
