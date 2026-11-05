package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;

public class LeaveBrigEventProcessor extends EventProcessor<LeaveBrigEvent> {

    @Override
    protected boolean isValid() {
        return galacticaBoard.charactersIn(BRIG).contains(player.character())
                && isEligibleDestination()
                && meetsDestinationLimit();
    }

    // todo if all human players are in the "Brig", the first one to leave becomes Admiral and CAG (rule 4.4)
    @Override
    public Followup process() {
        ops.player(player.number()).moveTo(event.destination());
        return Followup.NONE;
    }

    // As with normal movement, the character can't move to space, a hazardous location or a Cylon location.
    private boolean isEligibleDestination() {
        val destination = event.destination();
        return !destination.isSpaceLocation()
                && !destination.isHazardousLocation()
                && !destination.isCylonLocation();
    }

    private boolean meetsDestinationLimit() {
        return switch (event.destinationLimit()) {
            case null -> true;
            case DESTINATION_GALACTICA -> GALACTICA.locations().contains(event.destination());
            default -> throw new FrakCallTheAdmiralException();
        };
    }
}
