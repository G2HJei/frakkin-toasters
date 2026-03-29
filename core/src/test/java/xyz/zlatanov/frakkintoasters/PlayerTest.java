package xyz.zlatanov.frakkintoasters;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.character.Character.SAUL_TIGH;
import static xyz.zlatanov.frakkintoasters.skill.SkillCardType.REPAIR;

class PlayerTest {

    Player xo    = new Player(1).selectCharacter(SAUL_TIGH);
    Player chief = new Player(2).selectCharacter(CHIEF_GALEN_TYROL);

    @Test
    void shouldTrackSkillCards() {
        val cardToAdd = new SkillCard(1, REPAIR);
        xo.gainSkillCards(cardToAdd);
        assertTrue(xo.skillCards().contains(cardToAdd));
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

    @Test
    void shouldNotAllowExhaustingMiracleTokenIfNoneAvailable() {
        chief.exhaustMiracleToken();
        assertThrows(FrakCallTheAdmiralException.class, () -> chief.exhaustMiracleToken());
    }

}