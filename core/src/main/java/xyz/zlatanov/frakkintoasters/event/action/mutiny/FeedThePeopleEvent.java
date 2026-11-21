package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Feed the People (Mutiny Card)
// Action: +1 food, and the jump preparation track moves back 2. The card leaves the game.
public record FeedThePeopleEvent(int playerNumber) implements MutinyCardEvent {
}
