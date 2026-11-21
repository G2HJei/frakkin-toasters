package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import java.util.List;

// "Set the Agenda": the Quorum Cards of the President's hand that go to the bottom of the deck, the first one first: 2,
// or the one he holds.
public record SetTheAgendaChoiceEvent(int playerNumber, List<QuorumCard> cards) implements PlayerEvent {
}
