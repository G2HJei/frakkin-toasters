package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// At the "Administration", the President whose title is at stake may discard "Accept Prophecy" to keep it. Otherwise
// the title is replaced as without the card.
//
// @param administrationPlayerNumber the player of the "Administration", who chooses the new President
public record DefendPresidencyEvent(int playerNumber, boolean discardAcceptProphecy, int administrationPlayerNumber)
        implements PlayerEvent {
}
