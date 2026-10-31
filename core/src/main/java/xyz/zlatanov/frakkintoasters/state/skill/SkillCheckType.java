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
    POWER_FAILURE(LEADERSHIP, TACTICS, ENGINEERING);

    //todo add power
    private final List<SkillCardColor> skillCheckColors;

    SkillCheckType(SkillCardColor... colors) {
        skillCheckColors = Arrays.stream(colors)
                .sorted(comparing(Enum::ordinal))
                .toList();
    }
}
