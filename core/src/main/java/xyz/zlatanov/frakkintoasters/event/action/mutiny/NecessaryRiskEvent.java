package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// Necessary Risk (Mutiny Card)
// Action: +1 food, then a basestar and 3 raiders in a space area of the player's choice, as many as can be placed.
// The card leaves the game.
public record NecessaryRiskEvent(int playerNumber, Location area) implements MutinyCardEvent {
}
