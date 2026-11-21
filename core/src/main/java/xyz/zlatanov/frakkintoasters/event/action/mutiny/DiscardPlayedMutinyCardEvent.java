package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

// A played Mutiny Card goes to the discard pile, face up (rule A2.3.3), once what it queued before has resolved.
public record DiscardPlayedMutinyCardEvent(MutinyCard card) implements Event {
}
