package xyz.zlatanov.frakkintoasters.event.location;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ArmoryEventProcessor extends EventProcessor<ArmoryEvent> {

    //validate centurion with centurionId is on the boarding track
    @Override
    public Followup process() {
        val roll = rollDie();
        if (roll >= 7) {
            val destroyedCenturion = galacticaBoard.destroyCenturion(event.centurionId());
            game.cylonShips().returnedCenturion(destroyedCenturion);
        }
        return Followup.NONE;
    }
}
