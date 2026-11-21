package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.character.CharacterMoves;
import xyz.zlatanov.frakkintoasters.event.officials.BrigTitles;
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

    @Override
    public Followup process() {
        ops.player(player.number()).moveTo(event.destination());
        return BrigTitles.afterLeaving(game);
    }

    // As with normal movement, the character can't move to space, a hazardous location or a Cylon location.
    private boolean isEligibleDestination() {
        val destination = event.destination();
        return destination != null && !destination.isSpaceLocation()
                && !destination.isHazardousLocation()
                && !destination.isCylonLocation()
                && !game.isDestroyed(destination) // nobody goes back to a destroyed Colonial One or Pegasus
                && !CharacterMoves.cloudedBlocks(game, player, destination);
    }

    private boolean meetsDestinationLimit() {
        return switch (event.destinationLimit()) {
            case null -> true;
            case DESTINATION_GALACTICA -> GALACTICA.locations().contains(event.destination());
            default -> throw new FrakCallTheAdmiralException();
        };
    }
}
