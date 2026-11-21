package xyz.zlatanov.frakkintoasters.event.action.title;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The nuke hits this basestar, by its roll of 1 to 7: 1-2 damage it twice, 3-6 destroy it, 7 destroy it and 3 raiders in
// its space area (rule 11.6.1).
public record NukeBasestarEvent(int playerNumber, int basestarId, int result) implements PlayerEvent {
}
