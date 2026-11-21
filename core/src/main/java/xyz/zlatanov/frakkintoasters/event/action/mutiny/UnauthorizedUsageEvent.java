package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// Unauthorized Usage (Mutiny Card)
// Action: launch a nuke at a space area, then discard every nuke token (errata A2.3.3.2). The card leaves the game.
// Not when the Admiral has no nukes.
public record UnauthorizedUsageEvent(int playerNumber, Location area) implements MutinyCardEvent {
}
