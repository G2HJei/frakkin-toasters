package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.character.EscortEventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

public class DestroyCivilianShipEventProcessor extends EventProcessor<DestroyCivilianShipEvent> {

    @Override
    protected boolean isValid() {
        return galacticaBoard.shipsInSpace(CivilianShip.class).stream().anyMatch(s -> s.id() == event.shipId());
    }

    @Override
    public Followup process() {
        if (!event.escortOffered()) {
            // "Escort": before the ship is flipped over and destroyed
            val escort = EscortEventProcessor.offer(game, event.shipId());
            if (escort != Followup.NONE) {
                return escort;
            }
        }
        ops.ship(event.shipId()).destroy();
        return Followup.NONE;
    }
}
