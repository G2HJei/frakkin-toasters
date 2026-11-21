package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

// Unsavory Connections (Quorum Card)
// Action: the player discards 2 random Skill Cards and draws 2 Treachery Cards, then the fleet gains 1 food or 1 fuel.
// Then the card is discarded.
//
// @param resource FOOD or FUEL
public record UnsavoryConnectionsEvent(int playerNumber, Resource resource) implements QuorumCardEvent {
}
