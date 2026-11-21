package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;

// Gives a player a Super Crisis Card out of the Super Crisis draw pile, e.g. for a revealed Cylon to play at "Caprica".
public record GiveSuperCrisisCardToPlayerEvent(int targetPlayerNumber, SuperCrisisCard card) implements SandboxEvent {
}
