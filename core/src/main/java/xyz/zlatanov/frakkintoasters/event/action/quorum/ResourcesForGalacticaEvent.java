package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

// Resources for Galactica (Quorum Card)
// Action: repair up to 1 location and up to 2 damaged vipers, then the card is discarded.
//
// @param location a damaged location, or null
// @param viperIds up to 2 damaged vipers
public record ResourcesForGalacticaEvent(int playerNumber, Location location, List<Integer> viperIds)
        implements QuorumCardEvent {
}
