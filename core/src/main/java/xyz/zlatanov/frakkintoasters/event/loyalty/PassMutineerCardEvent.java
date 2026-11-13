package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The Mutineer reveals as a Cylon or is executed as one, and passes the "You Are the Mutineer" card to a human player of
// his choice, who draws a Mutiny Card and gives away his titles (rule 6.1.1).
public record PassMutineerCardEvent(int playerNumber, int recipientPlayerNumber) implements PlayerEvent {
}
