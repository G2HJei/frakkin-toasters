package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Accept Prophecy (Quorum Card)
// Action: the player draws 1 Skill Card of any color, and the card stays in play. When the "Admiral's Quarters"
// targets the President, its difficulty rises by 2 and the card is discarded. At the "Administration" the President
// may discard it to keep the title.
public record AcceptProphecyEvent(int playerNumber) implements QuorumCardEvent {
}
