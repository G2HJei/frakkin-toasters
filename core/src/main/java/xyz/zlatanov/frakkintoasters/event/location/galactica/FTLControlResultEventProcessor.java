package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.JumpingTheFleetEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class FTLControlResultEventProcessor extends EventProcessor<FTLControlResultEvent> {

    private static final int LOWEST_SAFE_ROLL = 7;

    @Override
    public Followup process() {
        if (game.turn().dieRoll().result() < LOWEST_SAFE_ROLL) {
            val lostPopulation = galacticaBoard.jumpPreparation().lostPopulation();
            ops.galactica().decreasePopulation(lostPopulation);
        }
        return single(new JumpingTheFleetEvent());
    }
}
