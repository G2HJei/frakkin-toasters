package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.LookAtAllLoyaltyCardsEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.DETECTOR_SABOTAGE;

class CylonDetectorEventProcessorTest extends EventTestHarness<CylonDetectorEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(GAIUS_BALTAR);
        player(2).character(WILLIAM_ADAMA);
        moveTo(RESEARCH_LAB, GAIUS_BALTAR);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        player(2).loyaltyCards().addOnTop(NOT_CYLON);
    }

    @Test
    void looksAtAllLoyaltyCardsOfAPlayer() {
        execute(new CylonDetectorEvent(1, 2));

        assertFollowup(new LookAtAllLoyaltyCardsEvent(1, 2));
    }

    @Test
    void notWhileDetectorSabotageForbidsLooks() {
        ops.keepCrisisCardInPlay(DETECTOR_SABOTAGE);

        assertInvalid(new CylonDetectorEvent(1, 2));
    }

    @Test
    void notWithoutAMiracleToken() {
        ops.player(1).loseMiracleTokens();

        assertInvalid(new CylonDetectorEvent(1, 2));
    }
}
