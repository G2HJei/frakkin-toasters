package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;

import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;

public class DamageHumanFighterEventProcessor extends EventProcessor<DamageHumanFighterEvent> {

    @Override
    public Followup process() {
        val ship = galacticaBoard.shipInSpace(event.shipId(), HumanFighter.class);
        galacticaBoard.remove(ship);
        val pilot = ship.pilot();
        if (pilot != null) {
            ship.pilot(null);
            game.moveTo(SICKBAY, pilot); //todo make this an event to track separately (with label)?
        }
        galacticaBoard.addToDamagedShips(ship);
        return Followup.NONE;
    }
}
