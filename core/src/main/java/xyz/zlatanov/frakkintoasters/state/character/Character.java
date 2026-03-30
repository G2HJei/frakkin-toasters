package xyz.zlatanov.frakkintoasters.state.character;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.skill.SkillSetOption;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.*;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum Character {
    AARON_DORAL(CYLON_LEADER, "1xTr, 1xPo/Ta", CAPRICA),
    ANASTASIA_DEE_DUALLA(SUPPORT, "1xL, 3xTa, 1xE", COMMUNICATIONS),
    BRENDAN_HOTDOG_COSTANZA(PILOT, "1xL, 1xTa, 2xPi, 1xE", HANGAR_DECK),
    CALLANDRA_CALLY_TYROL(SUPPORT, "1xPo, 1xL, 1xTa, 2xE", HANGAR_DECK),
    CAPRICA_SIX(CYLON_LEADER, "1xL, 1xTr/E", CAPRICA),
    CAVIL(CYLON_LEADER, "1xTa, 1xTr/E", CYLON_FLEET),
    CHIEF_GALEN_TYROL(SUPPORT, "1xPo, 2xL, 2xE", HANGAR_DECK),
    DANNA_BIERS(CYLON_LEADER, "1xPo/L, 1xTr/E", HUMAN_FLEET, PRESS_ROOM),
    ELLEN_TIGH(POLITICAL_LEADER, "2xPo, 2xL, 1xTr", ADMIRALS_QUARTERS),
    FELIX_GAETA(MILITARY_LEADER, "2xTa, 1xE, 2xL/Po", FTL_CONTROL),
    GAIUS_BALTAR(POLITICAL_LEADER, "2xPo, 1xL, 1xE", RESEARCH_LAB),
    GAIUS_BALTAR_ALT(SUPPORT, "2xPo, 2xL, 1xE", ADMIRALS_QUARTERS),
    HELENA_CAIN(MILITARY_LEADER, "2xL, 2xTa, 1xTa/L", PEGASUS_CIC, COMMAND),
    KARL_HELO_AGATHON(PILOT, "2xL, 2xTa, 1xPi"),
    KARL_HELO_AGATHON_ALT(PILOT, "2xL, 2xTa, 1xPi", ADMIRALS_QUARTERS, COMMAND),
    KARA_STARBUCK_THRACE(PILOT, "2xTa, 2xPi, 1xL/E", HANGAR_DECK),
    LAURA_ROSLIN(POLITICAL_LEADER, "3xPo, 2xL", PRESIDENTS_OFFICE),
    LEE_ADAMA(POLITICAL_LEADER, "1xTa, 2xPi, 2xL/Po", ADMIRALS_QUARTERS),
    LEE_APOLLO_ADAMA(PILOT, "1xTa, 2xPi, 2xL/Po"),
    LEOBEN_CONOY(CYLON_LEADER, "1xPo, 1xTr/E", HUMAN_FLEET),
    LOUANNE_KAT_KATRAINE(PILOT, "1xL, 2xTa, 2xPi", HANGAR_DECK),
    LOUIS_HOSHI(MILITARY_LEADER, "2xL, 2xTa, 1xE", COMMUNICATIONS),
    ROMO_LAMPKIN(POLITICAL_LEADER, "3xPo, 2xTa", ADMINISTRATION),
    SAMUEL_T_ANDERS(PILOT, "2xL, 2xTa, 1xTa/Pi", ARMORY),
    SAUL_TIGH(MILITARY_LEADER, "2xL, 3xTa", COMMAND),
    SHARON_ATHENA_AGATHON(CYLON_LEADER, "1xPi, 1xL/E", HANGAR_DECK),
    SHARON_BOOMER_VALERII(PILOT, "2xTa, 2xPi, 1xE", ARMORY),
    SHERMAN_DOC_COTTLE(SUPPORT, "1xPo, 2xTa, 2xE", RESEARCH_LAB),
    SIMON_ONEIL(CYLON_LEADER, "1xE, 1xTr/Ta", CYLON_FLEET),
    TOM_ZAREK(POLITICAL_LEADER, "2xPo, 2xL, 1xTa", ADMINISTRATION),
    TOM_ZAREK_ALT(MILITARY_LEADER, "2xPo, 2xL, 1xTa", WEAPONS_CONTROL),
    TORY_FOSTER(POLITICAL_LEADER, "3xPo, 1xL, 1xTa", PRESS_ROOM),
    WILLIAM_ADAMA(MILITARY_LEADER, "3xL, 2xTa", ADMIRALS_QUARTERS);

    private final CharacterType       type;
    private final Set<SkillSetOption> skillSet;
    private final Location[]          setup;

    Character(CharacterType type, String skills, Location... setup) {
        this.type = type;
        skillSet = SkillSetOption.skillSet(skills);
        this.setup = setup;
    }
}
