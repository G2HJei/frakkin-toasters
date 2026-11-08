package xyz.zlatanov.frakkintoasters.event.player;

import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.List;
import java.util.Map;

@Accessors(fluent = true)
public record DrawSkillCardsEvent(int playerNumber, Map<SkillCardColor, Integer> selection,
                                  EventConstraint drawLimit) implements PlayerEvent, ConstrainedEvent {

    public DrawSkillCardsEvent(int playerNumber, Map<SkillCardColor, Integer> selection) {
        this(playerNumber, selection, null);
    }

    @Override
    public DrawSkillCardsEvent withConstraints(List<EventConstraint> constraints) {
        return new DrawSkillCardsEvent(playerNumber, selection, ConstrainedEvent.atMostOne(constraints));
    }
}
