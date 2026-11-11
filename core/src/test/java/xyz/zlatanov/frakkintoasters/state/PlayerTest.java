package xyz.zlatanov.frakkintoasters.state;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
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

    @Test
    void shouldBeRevealedCylonOnlyAfterRevealingCylonLoyaltyCard() {
        xo.loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG);
        assertFalse(xo.isRevealedCylon());

        xo.loyaltyCards().reveal(CYLON_SEND_TO_BRIG);
        assertTrue(xo.isRevealedCylon());
        assertFalse(xo.isHuman());
    }

    @Test
    void shouldBeTheMutineerWithTheMutineerCardHiddenOrRevealed() {
        assertFalse(xo.isMutineer());

        xo.loyaltyCards().addOnTop(MUTINEER);
        assertTrue(xo.isMutineer());

        xo.loyaltyCards().reveal(MUTINEER);
        assertTrue(xo.isMutineer());
    }

    @Test
        //todo verify if this is correct when cylon leader is NOT infiltrating
    void shouldNotTreatCylonLeaderAsRevealedCylon() {
        val cavil = new Player(3).character(CAVIL);
        assertFalse(cavil.isRevealedCylon());
        assertFalse(cavil.isHuman());
    }

}