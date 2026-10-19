package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Arrays;
import java.util.List;

public record PlayCardsBeforeSkillCheckEvent(int playerNumber, List<SkillCard> skillCards) implements PlayerEvent {

    public PlayCardsBeforeSkillCheckEvent(int playerNumber, SkillCard... skillCards) {
        this(playerNumber, Arrays.stream(skillCards).toList());
    }
}
