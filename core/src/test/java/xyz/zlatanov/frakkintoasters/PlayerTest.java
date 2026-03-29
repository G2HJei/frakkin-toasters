package xyz.zlatanov.frakkintoasters;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.character.Character.SAUL_TIGH;
import static xyz.zlatanov.frakkintoasters.skill.SkillCardType.REPAIR;

class PlayerTest {

    Player xo    = new Player(SAUL_TIGH);
    Player chief = new Player(CHIEF_GALEN_TYROL);

    @Test
    void shouldTrackSkillCards() {
        xo.addSkillCards(REPAIR);
        assertEquals(List.of(REPAIR), xo.skillCards());
    }

    @Test
    void shouldTrackHandLimit() {
        assertEquals(10, xo.handLimit());
        assertEquals(8, chief.handLimit());
    }

    @Test
    void shouldTrackMiracleToken() {
        xo.exhaustMiracleToken();
        assertFalse(xo.hasMiracleToken());
        xo.gainMiracleToken();
        assertTrue(xo.hasMiracleToken());
    }

}