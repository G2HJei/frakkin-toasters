package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;

public class MoveFromBrigEventProcessor extends EventProcessor<MoveFromBrigEvent> {

    @Override
    protected boolean isValid() {
        val destination = event.destination();
        return isPlayer(event.targetPlayerNumber())
                && galacticaBoard.charactersIn(BRIG).contains(game.player(event.targetPlayerNumber()).character())
                && destination != null && GALACTICA.locations().contains(destination) && !destination.isHazardousLocation();
    }

    @Override
    public Followup process() {
        ops.player(event.targetPlayerNumber()).moveTo(event.destination());
        return Followup.NONE;
    }
}
