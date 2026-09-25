package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;

public class DamageHumanFighterEventProcessor extends EventProcessor<DamageHumanFighterEvent> {

    @Override
    public Followup process() {
        val ship = galacticaBoard.shipInSpace(event.shipId(), HumanFighter.class);
        galacticaBoard.remove(ship);
        galacticaBoard.addToDamagedShips(ship);
        return Followup.NONE;
    }
}
