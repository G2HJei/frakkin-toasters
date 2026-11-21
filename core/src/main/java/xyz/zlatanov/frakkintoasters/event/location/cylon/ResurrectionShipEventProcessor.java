package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;

public class ResurrectionShipEventProcessor extends LocationActionProcessor<ResurrectionShipEvent> {
    @Override
    protected Followup act() {
        ops.player(player.number()).drawSuperCrisisCard();
        return Followup.NONE;
    }
}
