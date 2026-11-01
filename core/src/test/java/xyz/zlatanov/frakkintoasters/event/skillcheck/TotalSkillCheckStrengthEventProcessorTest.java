package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class TotalSkillCheckStrengthEventProcessorTest extends EventTestHarness<TotalSkillCheckStrengthEvent> {

    private SkillCheck skillCheck;

    @BeforeEach
    void setUp() {
        skillCheck = game.startSkillCheck(ADMIRALS_QUARTERS); // leadership and tactics
    }

    @Test
    void shouldSubtractNonMatchingFromMatchingStrength() {
        skillCheck.matchingPile().addOnTop(List.of(new SkillCard(3, EXECUTIVE_ORDER), new SkillCard(4, LAUNCH_SCOUT)));
        skillCheck.nonMatchingPile().addOnTop(List.of(new SkillCard(2, CONSOLIDATE_POWER), new SkillCard(1, REPAIR)));

        execute(new TotalSkillCheckStrengthEvent());

        assertEquals(4, skillCheck.totalStrength());
        assertFollowup(declareEmergencyThenDetermineResult());
    }

    @Test
    void shouldAllowNegativeStrength() {
        skillCheck.matchingPile().addOnTop(new SkillCard(1, EXECUTIVE_ORDER));
        skillCheck.nonMatchingPile().addOnTop(new SkillCard(5, CONSOLIDATE_POWER));

        execute(new TotalSkillCheckStrengthEvent());

        assertEquals(-4, skillCheck.totalStrength());
        assertFollowup(declareEmergencyThenDetermineResult());
    }

    @Test
    void shouldCountEngineeringCardsAsPositiveWithScientificResearch() {
        skillCheck.playedBefore(1, List.of(new SkillCard(3, SCIENTIFIC_RESEARCH)));
        skillCheck.matchingPile().addOnTop(new SkillCard(2, EXECUTIVE_ORDER));
        skillCheck.nonMatchingPile().addOnTop(List.of(new SkillCard(3, REPAIR), new SkillCard(1, CONSOLIDATE_POWER)));

        execute(new TotalSkillCheckStrengthEvent());

        assertEquals(4, skillCheck.totalStrength());
        assertFollowup(declareEmergencyThenDetermineResult());
    }

    @Test
    void shouldCountTreacheryCardsAsPositiveWithAtAnyCost() {
        skillCheck.playedBefore(1, List.of(new SkillCard(3, AT_ANY_COST)));
        skillCheck.matchingPile().addOnTop(new SkillCard(2, EXECUTIVE_ORDER));
        skillCheck.nonMatchingPile().addOnTop(List.of(new SkillCard(5, EXPLOIT_A_WEAKNESS), new SkillCard(1, CONSOLIDATE_POWER)));

        execute(new TotalSkillCheckStrengthEvent());

        assertEquals(6, skillCheck.totalStrength());
        assertFollowup(declareEmergencyThenDetermineResult());
    }

    @Test
    void shouldUseDoubledEngineeringStrengthWithEstablishNetwork() {
        skillCheck.resolved(ESTABLISH_NETWORK);
        skillCheck.matchingPile().addOnTop(new SkillCard(5, EXECUTIVE_ORDER));
        skillCheck.nonMatchingPile().addOnTop(new SkillCard(2, REPAIR));

        execute(new TotalSkillCheckStrengthEvent());

        assertEquals(1, skillCheck.totalStrength());
        assertFollowup(declareEmergencyThenDetermineResult());
    }

    @Test
    void shouldAddOnePerZeroStrengthCardWithAllHandsOnDeck() {
        skillCheck.resolved(ALL_HANDS_ON_DECK);
        skillCheck.matchingPile().addOnTop(List.of(
                new SkillCard(0, ALL_HANDS_ON_DECK), new SkillCard(0, QUICK_THINKING), new SkillCard(3, EXECUTIVE_ORDER)));
        skillCheck.nonMatchingPile().addOnTop(List.of(new SkillCard(0, BAIT), new SkillCard(2, CONSOLIDATE_POWER)));

        execute(new TotalSkillCheckStrengthEvent());

        assertEquals(4, skillCheck.totalStrength());
        assertFollowup(declareEmergencyThenDetermineResult());
    }

    @Test
    void shouldLetEachPlayerDeclareEmergencyOrPassBeforeDeterminingResult() {
        execute(new TotalSkillCheckStrengthEvent());

        assertEquals(0, skillCheck.totalStrength());
        assertFollowup(declareEmergencyThenDetermineResult());
    }

    @Test
    void shouldOnlyDetermineResultWhenStrengthReachesPrintedDifficulty() {
        skillCheck.matchingPile().addOnTop(List.of(new SkillCard(3, EXECUTIVE_ORDER), new SkillCard(4, LAUNCH_SCOUT)));

        execute(new TotalSkillCheckStrengthEvent());

        assertEquals(7, skillCheck.totalStrength());
        assertFollowup(new DetermineSkillCheckResultEvent());
    }

    @Test
    void shouldNotAllowTotalingTwice() {
        skillCheck.totalStrength(3);
        assertInvalid(new TotalSkillCheckStrengthEvent());
    }

    @Test
    void shouldNotAllowWhenResultIsAlreadyDetermined() {
        skillCheck.result(PASS);
        assertInvalid(new TotalSkillCheckStrengthEvent());
    }

    private static Followup declareEmergencyThenDetermineResult() {
        return all(
                one(new PlayerDecisionEvent<>(1, DeclareEmergencyEvent.class), new NoOpEvent(1)),
                one(new PlayerDecisionEvent<>(2, DeclareEmergencyEvent.class), new NoOpEvent(2)),
                one(new PlayerDecisionEvent<>(3, DeclareEmergencyEvent.class), new NoOpEvent(3)),
                single(new DetermineSkillCheckResultEvent()));
    }
}
