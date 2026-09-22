package xyz.zlatanov.frakkintoasters.state.skill;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;

import static java.util.Comparator.comparing;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.LEADERSHIP;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;

@Getter
@Accessors(fluent = true)
public enum SkillCheck {
    ADMIRALS_QUARTERS(LEADERSHIP, TACTICS);

    private final List<SkillCardColor> skillCheckColors;

    SkillCheck(SkillCardColor... colors) {
        skillCheckColors = Arrays.stream(colors)
                .sorted(comparing(Enum::ordinal))
                .toList();
    }
}
