package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

public record DiscardSkillCardsEvent(int playerNumber, List<SkillCard> cards,
                                     EventConstraint discardLimit) implements PlayerEvent, ConstrainedEvent {

    public DiscardSkillCardsEvent(int playerNumber, List<SkillCard> cards) {
        this(playerNumber, cards, null);
    }

    @Override
    public DiscardSkillCardsEvent withConstraints(List<EventConstraint> constraints) {
        return new DiscardSkillCardsEvent(playerNumber, cards, ConstrainedEvent.atMostOne(constraints));
    }
}
