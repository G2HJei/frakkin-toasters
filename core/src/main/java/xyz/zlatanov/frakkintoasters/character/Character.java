package xyz.zlatanov.frakkintoasters.character;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.skill.SkillSetOption;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.character.CharacterType.*;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum Character {
    AARON_DORAL(CYLON_LEADER, "1xTr, 1xPo/Ta"),
    ANASTASIA_DEE_DUALLA(SUPPORT, "1xL, 3xTa, 1xE"),
    BRENDAN_HOTDOG_COSTANZA(PILOT, "1xL, 1xTa, 2xPi, 1xE"),
    CALLANDRA_CALLY_TYROL(SUPPORT, "1xPo, 1xL, 1xTa, 2xE"),
    CAPRICA_SIX(CYLON_LEADER, "1xL, 1xTr/E"),
    CAVIL(CYLON_LEADER, "1xTa, 1xTr/E"),
    CHIEF_GALEN_TYROL(SUPPORT, "1xPo, 2xL, 2xE"),
    DANNA_BIERS(CYLON_LEADER, "1xPo/L, 1xTr/E"),
    ELLEN_TIGH(POLITICAL_LEADER, "2xPo, 2xL, 1xTr"),
    FELIX_GAETA(MILITARY_LEADER, "2xTa, 1xE, 2xL/Po"),
    GAIUS_BALTAR(POLITICAL_LEADER, "2xPo, 1xL, 1xE"),
    GAIUS_BALTAR_ALT(SUPPORT, "2xPo, 2xL, 1xE"),
    HELENA_CAIN(MILITARY_LEADER, "2xL, 2xTa, 1xTa/L"),
    KARL_HELO_AGATHON(PILOT, "2xL, 2xTa, 1xPi"),
    KARL_HELO_AGATHON_ALT(PILOT, "2xL, 2xTa, 1xPi"),
    KARA_STARBUCK_THRACE(PILOT, "2xTa, 2xPi, 1xL/E"),
    LAURA_ROSLIN(POLITICAL_LEADER, "3xPo, 2xL"),
    LEE_ADAMA(POLITICAL_LEADER, "1xTa, 2xPi, 2xL/Po"),
    LEE_APOLLO_ADAMA(PILOT, "1xTa, 2xPi, 2xL/Po"),
    LEOBEN_CONOY(CYLON_LEADER, "1xPo, 1xTr/E"),
    LOUANNE_KAT_KATRAINE(PILOT, "1xL, 2xTa, 2xPi"),
    LOUIS_HOSHI(MILITARY_LEADER, "2xL, 2xTa, 1xE"),
    ROMO_LAMPKIN(POLITICAL_LEADER, "3xPo, 2xTa"),
    SAMUEL_T_ANDERS(PILOT, "2xL, 2xTa, 1xTa/Pi"),
    SAUL_TIGH(MILITARY_LEADER, "2xL, 3xTa"),
    SHARON_ATHENA_AGATHON(CYLON_LEADER, "1xPi, 1xL/E"),
    SHARON_BOOMER_VALERII(PILOT, "2xTa, 2xPi, 1xE"),
    SHERMAN_DOC_COTTLE(SUPPORT, "1xPo, 2xTa, 2xE"),
    SIMON_ONEIL(CYLON_LEADER, "1xE, 1xTr/Ta"),
    TOM_ZAREK(POLITICAL_LEADER, "2xPo, 2xL, 1xTa"),
    TOM_ZAREK_ALT(MILITARY_LEADER, "2xPo, 2xL, 1xTa"),
    TORY_FOSTER(POLITICAL_LEADER, "3xPo, 1xL, 1xTa"),
    WILLIAM_ADAMA(MILITARY_LEADER, "3xL, 2xTa");

    private final CharacterType       type;
    private final Set<SkillSetOption> skillSet;

    Character(CharacterType type, String skills) {
        this.type = type;
        skillSet = SkillSetOption.skillSet(skills);
    }
}
