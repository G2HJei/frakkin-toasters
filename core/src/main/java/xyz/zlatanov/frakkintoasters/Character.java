package xyz.zlatanov.frakkintoasters;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.skill.SkillSetOption;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.CharacterType.*;
import static xyz.zlatanov.frakkintoasters.skill.SkillSetOption.skills;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum Character {
    CHIEF_GALEN_TYROL(SUPPORT, skills("1xPo, 2xL, 2xE")),
    GAIUS_BALTAR(POLITICAL_LEADER, skills("2xPo, 1xL, 1xE")),
    KARA_STARBUCK_THRACE(PILOT, skills("2xTa, 2xPi, 1xL/E")),
    KARL_HELO_AGATHON(PILOT, skills("2xL, 2xTa, 1xPi")),
    LAURA_ROSLIN(POLITICAL_LEADER, skills("3xPo, 2xL")),
    LEE_APOLLO_ADAMA(PILOT, skills("1xTa, 2xPi, 2xL/Po")),
    SAUL_TIGH(MILITARY_LEADER, skills("2xL, 3xTa")),
    SHARON_BOOMER_VALERII(PILOT, skills("2xTa, 2xPi, 1xE")),
    TOM_ZAREK(POLITICAL_LEADER, skills("2xPo, 2xL, 1xTa")),
    WILLIAM_ADAMA(MILITARY_LEADER, skills("3xL, 2xTa"));

    private final CharacterType       type;
    private final Set<SkillSetOption> skillSet;

}
