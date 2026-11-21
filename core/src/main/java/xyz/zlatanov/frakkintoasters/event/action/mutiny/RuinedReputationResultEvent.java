package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Event;

// "Ruined Reputation", after the die roll: on 4 or less the chosen human goes to the "Brig".
public record RuinedReputationResultEvent(int targetPlayerNumber) implements Event {
}
