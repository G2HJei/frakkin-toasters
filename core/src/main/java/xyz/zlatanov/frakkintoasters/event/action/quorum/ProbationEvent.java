package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Probation (Quorum Card)
// Action: the card is given to a player, who keeps it in play. After he puts cards into a skill check, the President
// may discard it to look at them.
public record ProbationEvent(int playerNumber, int holderPlayerNumber) implements QuorumCardEvent {
}
