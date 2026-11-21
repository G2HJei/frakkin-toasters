package xyz.zlatanov.frakkintoasters.event.sandbox;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ChangeResourceEventProcessor extends EventProcessor<ChangeResourceEvent> {

    @Override
    protected boolean isValid() {
        if (event.resource() == null || event.amount() == 0) {
            return false;
        }
        val result = level() + event.amount();
        return result >= 0 && result <= 15; // rule 13.2.1
    }

    @Override
    public Followup process() {
        if (event.amount() > 0) {
            ops.galactica().increaseResource(event.resource(), event.amount());
        } else {
            ops.sandbox().decreaseResource(event.resource(), -event.amount());
        }
        return Followup.NONE;
    }

    private int level() {
        return switch (event.resource()) {
            case FUEL -> galacticaBoard.fuel();
            case FOOD -> galacticaBoard.food();
            case MORALE -> galacticaBoard.morale();
            case POPULATION -> galacticaBoard.population();
        };
    }
}
