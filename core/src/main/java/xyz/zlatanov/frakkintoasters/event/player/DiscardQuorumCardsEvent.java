package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import java.util.List;

public record DiscardQuorumCardsEvent(int playerNumber, List<QuorumCard> cards,
                                      EventConstraint discardLimit) implements PlayerEvent, ConstrainedEvent {

    public DiscardQuorumCardsEvent(int playerNumber, List<QuorumCard> cards) {
        this(playerNumber, cards, null);
    }

    @Override
    public DiscardQuorumCardsEvent withConstraints(List<EventConstraint> constraints) {
        return new DiscardQuorumCardsEvent(playerNumber, cards, ConstrainedEvent.atMostOne(constraints));
    }
}
