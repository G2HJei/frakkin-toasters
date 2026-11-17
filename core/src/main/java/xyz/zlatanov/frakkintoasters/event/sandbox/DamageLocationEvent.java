package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.board.Location;

// Damages a location of Galactica or Pegasus of the setup's choice, instead of a drawn damage token. The characters stay
// where they are. At 6 damaged locations of Galactica the Cylons win, as by the rules.
public record DamageLocationEvent(Location location) implements SandboxEvent {
}
