package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

import java.util.List;

// Authorization of Brutal Force (Quorum Card)
// Action: destroy 3 raiders, 1 heavy raider or 1 centurion, then roll a die. On 2 or less the fleet loses 1
// population. Then the card is discarded.
//
// @param shipIds 3 raiders (all of them when fewer are left), 1 heavy raider, or 1 centurion
public record AuthorizationOfBrutalForceEvent(int playerNumber, List<Integer> shipIds) implements QuorumCardEvent {
}
