package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.FTL_CONTROL;

// FTL Control (Galactica location)
// Action: Jump the fleet if the Jump Preparation track is not in the red zone.*
// *Might lose population.
public record FTLControlEvent(int playerNumber) implements LocationEvent {

    @Override
    public Location location() {
        return FTL_CONTROL;
    }
}
