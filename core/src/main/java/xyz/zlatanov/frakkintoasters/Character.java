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
    GAIUS_BALTAR_ALT(SUPPORT, skills("2xPo, 2xL, 1xE")),
    KARA_STARBUCK_THRACE(PILOT, skills("2xTa, 2xPi, 1xL/E")),
    KARL_HELO_AGATHON(PILOT, skills("2xL, 2xTa, 1xPi")),
    KARL_HELO_AGATHON_ALT(PILOT, skills("2xL, 2xTa, 1xPi")),
    LAURA_ROSLIN(POLITICAL_LEADER, skills("3xPo, 2xL")),
    LEE_APOLLO_ADAMA(PILOT, skills("1xTa, 2xPi, 2xL/Po")),
    LEE_ADAMA(POLITICAL_LEADER, skills("1xTa, 2xPi, 2xL/Po")),
    SAUL_TIGH(MILITARY_LEADER, skills("2xL, 3xTa")),
    SHARON_BOOMER_VALERII(PILOT, skills("2xTa, 2xPi, 1xE")),
    SHARON_ATHENA_AGATHON(CYLON_LEADER, skills("1xPi, 1xL/E")),
    TOM_ZAREK(POLITICAL_LEADER, skills("2xPo, 2xL, 1xTa")),
    TOM_ZAREK_ALT(MILITARY_LEADER, skills("2xPo, 2xL, 1xTa")),
    WILLIAM_ADAMA(MILITARY_LEADER, skills("3xL, 2xTa")),
    SIMON_ONEIL(CYLON_LEADER, skills("1xE, 1xTr/Ta")),
    AARON_DORAL(CYLON_LEADER, skills("1xTr, 1xPo/Ta")),
    ROMO_LAMPKIN(POLITICAL_LEADER, skills("3xPo, 2xTa")),
    BRENDAN_HOTDOG_COSTANZA(PILOT, skills("1xL, 1xTa, 2xPi, 1xE")),
    LOUIS_HOSHI(MILITARY_LEADER, skills("2xL, 2xTa, 1xE")),
    TORY_FOSTER(POLITICAL_LEADER, skills("3xPo, 1xL, 1xTa")),
    FELIX_GAETA(MILITARY_LEADER, skills("2xTa, 1xE, 2xL/Po")),
    LEOBEN_CONOY(CYLON_LEADER, skills("1xPo, 1xTr/E")),
    CAPRICA_SIX(CYLON_LEADER, skills("1xL, 1xTr/E")),
    ELLEN_TIGH(POLITICAL_LEADER, skills("2xPo, 2xL, 1xTr")),
    LOUANNE_KAT_KATRAINE(PILOT, skills("1xL, 2xTa, 2xPi")),
    HELENA_CAIN(MILITARY_LEADER, skills("2xL, 2xTa, 1xTa/L")),
    DANNA_BIERS(CYLON_LEADER, skills("1xPo/L, 1xTr/E")),
    SHERMAN_DOC_COTTLE(SUPPORT, skills("1xPo, 2xTa, 2xE")),
    CALLANDRA_CALLY_TYROL(SUPPORT, skills("1xPo, 1xL, 1xTa, 2xE")),
    SAMUEL_T_ANDERS(PILOT, skills("2xL, 2xTa, 1xTa/Pi")),
    ANASTASIA_DEE_DUALLA(SUPPORT, skills("1xL, 3xTa, 1xE")),
    CAVIL(CYLON_LEADER, skills("1xTa, 1xTr/E"));

    private final CharacterType       type;
    private final Set<SkillSetOption> skillSet;

}
