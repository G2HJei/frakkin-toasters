package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.POWER_FAILURE;

class PoliticalProwessEventProcessorTest extends EventTestHarness<PoliticalProwessEvent> {

    private static final SkillCard POLITICAL_PROWESS_6 = new SkillCard(6, POLITICAL_PROWESS);

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(1).gainSkillCards(POLITICAL_PROWESS_6);
    }

    @Test
    void shouldAutomaticallyPass() {
        game.startSkillCheck(ADMIRALS_QUARTERS);

        execute(new PoliticalProwessEvent(1, POLITICAL_PROWESS_6, true));

        assertNoSkillCards(1);
        assertEquals(List.of(POLITICAL_PROWESS_6), politicsDeck.discardedCards());
        assertEquals(PASS, game.activeSkillCheck().result());
    }

    @Test
    void shouldAutomaticallyFail() {
        game.startSkillCheck(ADMIRALS_QUARTERS);

        execute(new PoliticalProwessEvent(1, POLITICAL_PROWESS_6, false));

        assertEquals(FAIL, game.activeSkillCheck().result());
    }

    @Test
    void shouldNotAllowSkillCheckNotTriggeredByLocation() {
        game.startSkillCheck(POWER_FAILURE);
        assertInvalid(new PoliticalProwessEvent(1, POLITICAL_PROWESS_6, true));
    }

    @Test
    void shouldNotAllowAfterCardsArePlayedBeforeSkillCheck() {
        game.startSkillCheck(ADMIRALS_QUARTERS)
                .playedBefore(2, List.of(new SkillCard(3, INVESTIGATIVE_COMMITTEE)));
        assertInvalid(new PoliticalProwessEvent(1, POLITICAL_PROWESS_6, true));
    }

    @Test
    void shouldNotAllowAfterCardsAreAddedToSkillCheck() {
        game.startSkillCheck(ADMIRALS_QUARTERS)
                .cards().addOnTop(new SkillCard(3, DECLARE_EMERGENCY));
        assertInvalid(new PoliticalProwessEvent(1, POLITICAL_PROWESS_6, true));
    }

    @Test
    void shouldNotAllowAfterCardsAreDivided() {
        game.startSkillCheck(ADMIRALS_QUARTERS)
                .matchingPile().addOnTop(new SkillCard(3, DECLARE_EMERGENCY));
        assertInvalid(new PoliticalProwessEvent(1, POLITICAL_PROWESS_6, true));
    }

    @Test
    void shouldNotAllowAfterResultIsDetermined() {
        game.startSkillCheck(ADMIRALS_QUARTERS).result(FAIL);
        assertInvalid(new PoliticalProwessEvent(1, POLITICAL_PROWESS_6, true));
    }

    @Test
    void shouldNotAllowCardNotInHand() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
        assertInvalid(new PoliticalProwessEvent(2, POLITICAL_PROWESS_6, true));
    }

    @Test
    void shouldNotAllowOtherCardType() {
        val consolidatePower = new SkillCard(6, CONSOLIDATE_POWER);
        player(1).gainSkillCards(consolidatePower);
        game.startSkillCheck(ADMIRALS_QUARTERS);

        assertInvalid(new PoliticalProwessEvent(1, consolidatePower, true));
    }
}
