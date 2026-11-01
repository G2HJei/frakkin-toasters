package xyz.zlatanov.frakkintoasters.state.skill;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;

import static java.util.Comparator.comparing;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;

@Getter
@Accessors(fluent = true)
public enum SkillCheckType {
    ADMIRALS_QUARTERS(LEADERSHIP, TACTICS),
    DIVISIVE_BEHAVIOR(POLITICS, LEADERSHIP, TACTICS),
    HAUNTED_BY_THE_PAST(POLITICS, LEADERSHIP),
    IN_THE_RING(POLITICS, LEADERSHIP, TACTICS),
    POWER_FAILURE(LEADERSHIP, TACTICS, ENGINEERING),
    THREAT_OF_SUPER_NOVA(LEADERSHIP, PILOTING, ENGINEERING),
    FIRE_ALL_MISSILES(TACTICS, PILOTING);

    //todo add power
    private final List<SkillCardColor> skillCheckColors;

    SkillCheckType(SkillCardColor... colors) {
        skillCheckColors = Arrays.stream(colors)
                .sorted(comparing(Enum::ordinal))
                .toList();
    }
}
