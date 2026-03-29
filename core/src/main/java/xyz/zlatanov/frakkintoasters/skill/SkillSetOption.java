package xyz.zlatanov.frakkintoasters.skill;

import lombok.val;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static xyz.zlatanov.frakkintoasters.skill.SkillCardColor.*;

public record SkillSetOption(int count, Set<SkillCardColor> availableTypes) {
    public static Set<SkillSetOption> skillSet(String skillSet) {
        val skillSetOptions = new HashSet<SkillSetOption>();
        for (var option : skillSet.split(",")) {
            skillSetOptions.add(parse(option.trim()));
        }
        return skillSetOptions;
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
                .collect(Collectors.toSet());
        return new SkillSetOption(count, skillTypes);
    }
}
