package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// Romo Lampkin's "Attorney", once per game: a character leaves the "Brig" for a Galactica location that isn't
// hazardous, and if he is another player's, Romo takes that player's Skill Cards.
public record AttorneyEvent(int playerNumber, int targetPlayerNumber, Location destination) implements CharacterActionEvent {
}
