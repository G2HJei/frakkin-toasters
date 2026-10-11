package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ArmoryEventProcessor extends EventProcessor<ArmoryEvent> {

    //validate centurion with centurionId is on the boarding track
    @Override
    public Followup process() {
        val roll = ops.rollDie();
        if (roll >= 7) {
            ops.galactica().destroyCenturion(event.centurionId());
        }
        return Followup.NONE;
    }
}
