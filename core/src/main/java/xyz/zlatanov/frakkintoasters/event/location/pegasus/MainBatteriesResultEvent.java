package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// What "Main Batteries" does to the chosen space area, after the die roll:
// 1: Destroy 1 civilian ship
// 2-3: Damage 1 viper
// 4-6: Destroy 2 raiders
// 7-8: Destroy 4 raiders
public record MainBatteriesResultEvent(Location spaceLocation) implements Event {
}
