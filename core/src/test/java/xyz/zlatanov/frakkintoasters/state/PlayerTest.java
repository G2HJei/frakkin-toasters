package xyz.zlatanov.frakkintoasters.state;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.RECKLESS;
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

    // "A player can have only one miracle token at any time" (rule 4.3.1).
    @Test
    void shouldHoldOneMiracleToken() {
        assertEquals(1, xo.miracleTokens());
        assertFalse(xo.canGainMiracleToken());
        assertThrows(FrakCallTheAdmiralException.class, () -> xo.gainMiracleToken());
    }

    // Gaius Baltar (alt.) holds up to three (rule 4.3.1.1).
    @Test
    void baltarHoldsThreeMiracleTokens() {
        val baltar = new Player(3).character(GAIUS_BALTAR_ALT);
        baltar.gainMiracleToken();
        baltar.gainMiracleToken();

        assertEquals(3, baltar.miracleTokens());
        assertFalse(baltar.canGainMiracleToken());
    }

    // "Revealed Cylon players cannot gain miracle tokens" (rule 4.3.2).
    @Test
    void aRevealedCylonCantGainAMiracleToken() {
        xo.exhaustMiracleToken();
        xo.loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG).reveal(CYLON_SEND_TO_BRIG);

        assertFalse(xo.canGainMiracleToken());
    }

    // A revealed Cylon's abilities are ignored (rule 6.1.2.2), and he discards down to 10 (rule 6.1.2.3).
    @Test
    void aRevealedChiefHasTheUsualHandLimit() {
        chief.loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG).reveal(CYLON_SEND_TO_BRIG);

        assertFalse(chief.hasAbility(RECKLESS));
        assertEquals(10, chief.handLimit());
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