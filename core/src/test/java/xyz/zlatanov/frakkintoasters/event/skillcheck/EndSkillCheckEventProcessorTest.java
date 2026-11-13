package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class EndSkillCheckEventProcessorTest extends EventTestHarness<EndSkillCheckEvent> {

    private static final SkillCard JURY_RIGGED_1   = new SkillCard(1, JURY_RIGGED);
    private static final SkillCard LAUNCH_SCOUT_3  = new SkillCard(3, LAUNCH_SCOUT);
    private static final SkillCard REPAIR_2        = new SkillCard(2, REPAIR);
    private static final SkillCard POLITICAL_PROWESS_6 = new SkillCard(6, POLITICAL_PROWESS);

    // "All Skill Cards contributed toward the skill check are then discarded into the appropriate discard piles."
    @Test
    void shouldDiscardTheCardsOfTheCheckAndEndIt() {
        val skillCheck = game.startSkillCheck(ADMIRALS_QUARTERS).playedBefore(2, List.of(JURY_RIGGED_1));
        skillCheck.matchingPile().addOnTop(LAUNCH_SCOUT_3);
        skillCheck.nonMatchingPile().addOnTop(REPAIR_2);
        skillCheck.result(PASS);

        execute(new EndSkillCheckEvent());

        assertNull(game.activeSkillCheck());
        assertEquals(List.of(JURY_RIGGED_1, REPAIR_2), engineeringDeck.discardedCards());
        assertEquals(List.of(LAUNCH_SCOUT_3), tacticsDeck.discardedCards());
    }

    // Cards are left face down only when "Political Prowess" decided the check before it was made.
    @Test
    void shouldDiscardFaceDownCards() {
        game.startSkillCheck(ADMIRALS_QUARTERS).result(FAIL);
        game.activeSkillCheck().cards().addOnTop(POLITICAL_PROWESS_6);

        execute(new EndSkillCheckEvent());

        assertNull(game.activeSkillCheck());
        assertEquals(List.of(POLITICAL_PROWESS_6), politicsDeck.discardedCards());
    }

    @Test
    void shouldNotEndACheckWithoutAResult() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
        assertInvalid(new EndSkillCheckEvent());
    }

    @Test
    void shouldNotAllowWithoutASkillCheck() {
        assertInvalid(new EndSkillCheckEvent());
    }
}
