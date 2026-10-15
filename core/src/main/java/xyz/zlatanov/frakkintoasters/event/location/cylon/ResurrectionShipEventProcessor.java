package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ResurrectionShipEventProcessor extends EventProcessor<ResurrectionShipEvent> {
    @Override
    public Followup process() {
        ops.player(player.number()).drawSuperCrisisCard();
        return Followup.NONE;
    }
}
