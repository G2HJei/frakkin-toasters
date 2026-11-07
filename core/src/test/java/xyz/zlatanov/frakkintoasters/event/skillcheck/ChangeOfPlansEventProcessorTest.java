package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class ChangeOfPlansEventProcessorTest extends EventTestHarness<ChangeOfPlansEvent> {

    private static final SkillCard CHANGE_OF_PLANS_5 = new SkillCard(5, CHANGE_OF_PLANS);

    private SkillCheck skillCheck;

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(CAVIL);
        player(3).character(SAMUEL_T_ANDERS);
        player(3).gainSkillCards(CHANGE_OF_PLANS_5);
        skillCheck = game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldLetEachHumanPlayerDraw2InsteadOfPassResult() {
        skillCheck.result(PASS);

        execute(new ChangeOfPlansEvent(3, CHANGE_OF_PLANS_5));

        assertNoSkillCards(3);
        assertEquals(List.of(CHANGE_OF_PLANS_5), leadershipDeck.discardedCards());
        assertTrue(skillCheck.passResultReplaced());
        assertEquals(PASS, skillCheck.result());
        assertFollowup(all(
                new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_2),
                new PlayerDecisionEvent<>(3, DrawSkillCardsEvent.class, DRAW_EXACTLY_2)));
    }

    @Test
    void shouldAllowAfterPoliticalProwessPassedTheCheck() {
        val politicalProwess = new SkillCard(6, POLITICAL_PROWESS);
        player(1).gainSkillCards(politicalProwess);
        new PoliticalProwessEventProcessor().execute(game, new PoliticalProwessEvent(1, politicalProwess, true), ops);

        execute(new ChangeOfPlansEvent(3, CHANGE_OF_PLANS_5));

        assertTrue(skillCheck.passResultReplaced());
        assertFollowup(all(
                new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_2),
                new PlayerDecisionEvent<>(3, DrawSkillCardsEvent.class, DRAW_EXACTLY_2)));
    }

    @Test
    void shouldNotAllowBeforeResultIsDetermined() {
        assertInvalid(new ChangeOfPlansEvent(3, CHANGE_OF_PLANS_5));
    }

    @Test
    void shouldNotAllowWhenSkillCheckFailed() {
        skillCheck.result(FAIL);
        assertInvalid(new ChangeOfPlansEvent(3, CHANGE_OF_PLANS_5));
    }

    @Test
    void shouldNotAllowWhenPassResultAlreadyReplaced() {
        skillCheck.result(PASS).replacePassResult();
        assertInvalid(new ChangeOfPlansEvent(3, CHANGE_OF_PLANS_5));
    }

    @Test
    void shouldNotAllowCardNotInHand() {
        skillCheck.result(PASS);
        assertInvalid(new ChangeOfPlansEvent(1, CHANGE_OF_PLANS_5));
    }

    @Test
    void shouldNotAllowOtherCardType() {
        val declareEmergency = new SkillCard(5, DECLARE_EMERGENCY);
        player(3).gainSkillCards(declareEmergency);
        skillCheck.result(PASS);

        assertInvalid(new ChangeOfPlansEvent(3, declareEmergency));
    }
}
