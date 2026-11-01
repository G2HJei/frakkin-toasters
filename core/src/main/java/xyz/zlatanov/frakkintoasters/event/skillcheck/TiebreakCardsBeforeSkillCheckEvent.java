package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.skill.PlayedCard;

import java.util.Arrays;
import java.util.List;

public record TiebreakCardsBeforeSkillCheckEvent(List<PlayedCard> keptCards) implements Event {

    public TiebreakCardsBeforeSkillCheckEvent(PlayedCard... keptCards) {
        this(Arrays.stream(keptCards).toList());
    }
}
