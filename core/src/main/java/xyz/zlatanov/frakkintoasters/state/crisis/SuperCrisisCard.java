package xyz.zlatanov.frakkintoasters.state.crisis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisChooser.*;

/**
 * A Super Crisis Card, with what is printed on it, as {@link CrisisCard} has: its skill check, or for the three cards
 * without one, the title holder who chooses a section. None has a Cylon activation or a "prepare for jump" icon.
 */
@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum SuperCrisisCard {
    FIGHTING_BLIND(null, CAG),
    HUMAN_PRISONER(SkillCheckType.HUMAN_PRISONER, null),
    FIRE_ALL_MISSILES(SkillCheckType.FIRE_ALL_MISSILES, null),
    THE_FARM(SkillCheckType.THE_FARM, null),
    PSYCHOLOGICAL_WARFARE(null, PRESIDENT),
    FOOTAGE_TRANSMITTED(SkillCheckType.FOOTAGE_TRANSMITTED, null),
    DEMAND_PEACE_MANIFESTO(null, ADMIRAL),
    INBOUND_NUKES(SkillCheckType.INBOUND_NUKES, null),
    FLEET_MOBILIZATION(SkillCheckType.FLEET_MOBILIZATION, null),
    CYLON_INTRUDERS(SkillCheckType.CYLON_INTRUDERS, null),
    BOMB_ON_COLONIAL_1(SkillCheckType.BOMB_ON_COLONIAL_1, null);

    private final SkillCheckType skillCheck;
    private final CrisisChooser  chooser;
}
