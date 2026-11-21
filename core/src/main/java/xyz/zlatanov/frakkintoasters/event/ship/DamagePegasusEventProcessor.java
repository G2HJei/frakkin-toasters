package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class DamagePegasusEventProcessor extends EventProcessor<DamagePegasusEvent> {

    @Override
    protected boolean isValid() {
        return !pegasusBoard.destroyed();
    }

    @Override
    public Followup process() {
        ops.pegasus().damage();
        return DestroyPegasusEventProcessor.isWrecked(game) ? single(new DestroyPegasusEvent()) : NONE;
    }
}
