package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;

public class MoveFromBrigEventProcessor extends EventProcessor<MoveFromBrigEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber())
                && galacticaBoard.charactersIn(BRIG).contains(game.player(event.targetPlayerNumber()).character())
                && isValidDestination(event.destination());
    }

    // A Galactica location that isn't hazardous, e.g. for a "Presidential Pardon".
    public static boolean isValidDestination(Location destination) {
        return destination != null && GALACTICA.locations().contains(destination) && !destination.isHazardousLocation();
    }

    @Override
    public Followup process() {
        ops.player(event.targetPlayerNumber()).moveTo(event.destination());
        return Followup.NONE;
    }
}
