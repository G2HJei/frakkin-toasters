package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

// Brig (Galactica location) - skill check difficulty: 7, colors: POLITICS, TACTICS
// You may not move, draw Crisis Cards, or add more than 1 card to skill checks.
// Action: Pass this skill check to move to any location. (Any location on Galactica, rule 9.2.4.)
public record BrigEvent(int playerNumber) implements LocationEvent {

    @Override
    public Location location() {
        return BRIG;
    }
}
