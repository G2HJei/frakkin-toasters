package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.MAIN_BATTERIES;

// Main Batteries (Pegasus location)
// Action: Choose a space area to affect and roll a die:
// 1: Destroy 1 civilian ship
// 2-3: Damage 1 viper
// 4-6: Destroy 2 raiders
// 7-8: Destroy 4 raiders
public record MainBatteriesEvent(int playerNumber, Location spaceLocation) implements ActionEvent {

    @Override
    public Location location() {
        return MAIN_BATTERIES;
    }
}
