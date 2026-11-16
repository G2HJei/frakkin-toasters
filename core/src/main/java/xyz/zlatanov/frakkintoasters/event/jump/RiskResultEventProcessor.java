package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent.Area.RESERVES;

public class RiskResultEventProcessor extends EventProcessor<RiskResultEvent> {

    @Override
    protected boolean isValid() {
        return event.risk() != null;
    }

    @Override
    public Followup process() {
        val risk = event.risk();
        if (game.turn().dieRoll().result() >= risk.target()) {
            return single(new GainResourceEvent(risk.reward(), risk.amount()));
        }
        return single(risk.vipers() ? new DamageVipersEvent(risk.count(), RESERVES) : new DestroyRaptorEvent());
    }
}
