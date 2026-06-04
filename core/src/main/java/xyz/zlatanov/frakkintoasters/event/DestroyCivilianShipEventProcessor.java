package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

public class DestroyCivilianShipEventProcessor extends EventProcessor<DestroyCivilianShipEvent> {
    @Override
    public Followup process() {
        val civilianShip = galacticaBoard.shipInSpace(event.shipId(), CivilianShip.class);
        galacticaBoard
                .decreaseFuel(civilianShip.fuelCost())
                .decreaseMorale(civilianShip.moraleCost())
                .decreasePopulation(civilianShip.populationCost());
        game.removeComponent(civilianShip);
        return Followup.NONE;
    }
}
