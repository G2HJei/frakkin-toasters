package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESS_ROOM;

// Press Room (Colonial One location, Daybreak)
// Action: Choose another player to draw 1 Mutiny Card. (He does not move to the "Brig.") He keeps 1 of his Mutiny
// Cards and discards the rest. You may then discard a Mutiny Card.
public record PressRoomEvent(int playerNumber, int targetPlayer) implements ActionEvent {

    @Override
    public Location location() {
        return PRESS_ROOM;
    }
}
