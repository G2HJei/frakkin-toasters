package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

// A played Quorum Card goes to the discard pile, face up (rule A2.3.13), once what it queued has resolved.
public record DiscardPlayedQuorumCardEvent(QuorumCard card) implements Event {
}
