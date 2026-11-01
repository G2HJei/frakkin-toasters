package xyz.zlatanov.frakkintoasters.state.skill;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;

@Getter
@Accessors(fluent = true)
public enum SkillCheckType {
    ADMIRALS_QUARTERS(7, LEADERSHIP, TACTICS),
    DIVISIVE_BEHAVIOR(10, POLITICS, LEADERSHIP, TACTICS),
    HAUNTED_BY_THE_PAST(12, POLITICS, LEADERSHIP),
    IN_THE_RING(12, POLITICS, LEADERSHIP, TACTICS),
    POWER_FAILURE(14, LEADERSHIP, TACTICS, ENGINEERING),
    THREAT_OF_SUPER_NOVA(10, LEADERSHIP, PILOTING, ENGINEERING),
    FIRE_ALL_MISSILES(22, TACTICS, PILOTING);

    /**
     * The printed difficulty. Cards like "Declare Emergency" and "Jury Rigged" reduce it only for the active skill check.
     */
    private final int                  difficulty;
    /**
     * The printed partial pass number, {@code null} when the skill check has no partial pass result.
     */
    private final Integer              partialPass;
    private final List<SkillCardColor> skillCheckColors;

    //todo add the Brig, the Airlock, Resistance HQ and Detention once they have skill check types
    private static final Set<SkillCheckType> LOCATION_SKILL_CHECKS = Set.of(ADMIRALS_QUARTERS);

    SkillCheckType(int difficulty, SkillCardColor... colors) {
        this(difficulty, null, colors);
    }

    SkillCheckType(int difficulty, Integer partialPass, SkillCardColor... colors) {
        this.difficulty = difficulty;
        this.partialPass = partialPass;
        skillCheckColors = Arrays.stream(colors)
                .sorted(comparing(Enum::ordinal))
                .toList();
    }

    public boolean triggeredByLocation() {
        return LOCATION_SKILL_CHECKS.contains(this);
    }
}
