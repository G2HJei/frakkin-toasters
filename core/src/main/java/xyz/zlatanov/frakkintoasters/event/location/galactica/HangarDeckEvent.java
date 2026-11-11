package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;

// Hangar Deck (Galactica location)
// Action: Launch yourself in a viper. You may then take 1 more action.
public record HangarDeckEvent(int playerNumber) implements LocationEvent {

    @Override
    public Location location() {
        return HANGAR_DECK;
    }
}
