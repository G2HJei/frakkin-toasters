package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCenturionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class ArmoryResultEventProcessor extends EventProcessor<ArmoryResultEvent> {

    private static final int LOWEST_ROLL_TO_DESTROY = 7;

    @Override
    protected boolean isValid() {
        return galacticaBoard.boardingPartyTrack().keySet().stream().anyMatch(c -> c.id() == event.centurionId());
    }

    @Override
    public Followup process() {
        if (game.turn().dieRoll().result() < LOWEST_ROLL_TO_DESTROY) {
            return NONE;
        }
        return single(new DestroyCenturionEvent(event.centurionId()));
    }
}
