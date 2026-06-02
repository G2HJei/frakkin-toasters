package xyz.zlatanov.frakkintoasters.state;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.REPAIR;

class PlayerTest {

    Player xo    = new Player(1).character(SAUL_TIGH);
    Player chief = new Player(2).character(CHIEF_GALEN_TYROL);

    @Test
    void shouldTrackSkillCards() {
        val cardToAdd = new SkillCard(1, REPAIR);
        xo.gainSkillCards(cardToAdd);
        assertTrue(xo.skillCards().cards().contains(cardToAdd));
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