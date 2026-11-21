package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.officials.BrigTitles;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;

public class MoveFromBrigEventProcessor extends EventProcessor<MoveFromBrigEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber())
                && galacticaBoard.charactersIn(BRIG).contains(game.player(event.targetPlayerNumber()).character())
                && isGalacticaDestination(event.destination());
    }

    // A Galactica location that isn't hazardous, e.g. for a "Presidential Pardon".
    public static boolean isValidDestination(Location destination) {
        return isGalacticaDestination(destination) && !destination.isHazardousLocation();
    }

    // Any other Galactica location, hazardous ones included, e.g. for "Make a Deal". The card that moves the character
    // checks its own limits.
    public static boolean isGalacticaDestination(Location destination) {
        return destination != null && destination != BRIG && GALACTICA.locations().contains(destination);
    }

    @Override
    public Followup process() {
        ops.player(event.targetPlayerNumber()).moveTo(event.destination());
        return BrigTitles.afterLeaving(game);
    }
}
