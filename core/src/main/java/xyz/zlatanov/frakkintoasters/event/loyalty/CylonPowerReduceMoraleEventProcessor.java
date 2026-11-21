package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;

public class CylonPowerReduceMoraleEventProcessor extends EventProcessor<CylonPowerReduceMoraleEvent> {

    @Override
    public Followup process() {
        return single(lose(MORALE, 1));
    }
}
