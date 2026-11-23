package xyz.zlatanov.frakkintoasters.state.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;

public record SkillSetOption(int count, Set<SkillCardColor> colors) {

    // The options and their colors in the order the character sheet prints them, which the table's mats show.
    public static Set<SkillSetOption> skillSet(String skillSet) {
        val skillSetOptions = new LinkedHashSet<SkillSetOption>();
        for (var option : skillSet.split(",")) {
            skillSetOptions.add(parse(option.trim()));
        }
        return Collections.unmodifiableSet(skillSetOptions);
    }

    private static SkillSetOption parse(String option) {
        option = option.trim();
        val count = Integer.parseInt(option.charAt(0) + "");
        val skillTypes = Arrays.stream(option.substring(2).split("/"))
                .map(st -> switch (st) {
                    case "Po" -> POLITICS;
                    case "L" -> LEADERSHIP;
                    case "Ta" -> TACTICS;
                    case "Pi" -> PILOTING;
                    case "E" -> ENGINEERING;
                    case "Tr" -> TREACHERY;
                    default -> throw new IllegalArgumentException();
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new SkillSetOption(count, Collections.unmodifiableSet(skillTypes));
    }
}
