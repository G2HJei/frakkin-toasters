package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.Event;

import java.util.List;

// The raiders of an activation that haven't activated yet. They activate one entire space area at a time, in the order
// the current player chooses (rule 11.5).
public record ActivateRaiderAreasEvent(List<Integer> raiderShipIds) implements Event {
}
