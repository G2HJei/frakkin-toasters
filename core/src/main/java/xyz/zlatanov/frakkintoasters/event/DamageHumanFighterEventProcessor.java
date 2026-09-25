package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;

public class DamageHumanFighterEventProcessor extends EventProcessor<DamageHumanFighterEvent> {

    @Override
    public Followup process() {
        if (!damageInReserves()) {
            damageInSpace();
        }
        return Followup.NONE;
    }

    private boolean damageInReserves() {
        val humanFighter = galacticaBoard.removeFromReserves(event.shipId());
        if (humanFighter.isEmpty()) {
            return false;
        }
        galacticaBoard.addToDamagedShips((Viper) humanFighter.get());
        return true;
    }

    private void damageInSpace() {
        val ship = galacticaBoard.shipInSpace(event.shipId(), HumanFighter.class);
        galacticaBoard.remove(ship);
        val pilot = ship.pilot();
        if (pilot != null) {
            ship.pilot(null);
            game.moveTo(SICKBAY, pilot); //todo make this an event to track separately (with label)? or find another way to collect the label
        }
        if (ship instanceof Viper) {
            galacticaBoard.addToDamagedShips((Viper) ship);
        } else {
            game.removeComponent(ship);
        }
    }
}
