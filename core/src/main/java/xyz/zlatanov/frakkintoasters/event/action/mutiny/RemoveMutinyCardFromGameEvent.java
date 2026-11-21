package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

// A played Mutiny Card leaves the game, e.g. "Feed the People", once what it queued before has resolved.
public record RemoveMutinyCardFromGameEvent(MutinyCard card) implements Event {
}
