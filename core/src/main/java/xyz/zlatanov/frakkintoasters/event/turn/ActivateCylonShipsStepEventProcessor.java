package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTIVATE_CYLON_SHIPS;

public class ActivateCylonShipsStepEventProcessor extends EventProcessor<ActivateCylonShipsStepEvent> {

    @Override
    public Followup process() {
        if (!game.currentPlayer().isHuman()) {
            // Revealed Cylons have no Activate Cylon Ships step (rules 8.6, 6.1.3).
            return NONE;
        }
        ops.changeStep(ACTIVATE_CYLON_SHIPS);
        // todo activate the Cylon ships as the icon on the Crisis Card drawn this turn shows
        return NONE;
    }
}
