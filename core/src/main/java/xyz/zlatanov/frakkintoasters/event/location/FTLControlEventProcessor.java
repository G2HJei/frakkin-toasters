package xyz.zlatanov.frakkintoasters.event.location;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.CylonVictoryEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.JumpingTheFleetEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class FTLControlEventProcessor extends EventProcessor<FTLControlEvent> {
    @Override
    public Followup process() {
        val roll = rollDie();
        if (roll < 7) {
            val lostPop = galacticaBoard.jumpPreparation().lostPopulation();
            galacticaBoard.decreasePopulation(lostPop);
        }
        return galacticaBoard.population() > 0
                ? single(new JumpingTheFleetEvent())
                : single(new CylonVictoryEvent());
    }
}
