package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.AIRLOCK;

// Airlock (Pegasus location) - skill check difficulty: 12, colors: POLITICS, TACTICS, TREACHERY
// Action: Choose a character and pass this skill check to execute him. Reduce the difficulty of this check by 4 if the
// character is in the "Brig."
public record AirlockEvent(int playerNumber, int targetPlayer) implements LocationEvent {

    @Override
    public Location location() {
        return AIRLOCK;
    }
}
