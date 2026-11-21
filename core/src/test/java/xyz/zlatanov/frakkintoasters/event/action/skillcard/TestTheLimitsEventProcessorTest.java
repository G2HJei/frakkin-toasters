package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.IncreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.TEST_THE_LIMITS;

class TestTheLimitsEventProcessorTest extends EventTestHarness<TestTheLimitsEvent> {

    private static final SkillCard TEST_THE_LIMITS_5 = new SkillCard(5, TEST_THE_LIMITS);

    @BeforeEach
    void setUp() {
        player(1).character(CHIEF_GALEN_TYROL);
        player(1).gainSkillCards(TEST_THE_LIMITS_5);
    }

    @Test
    void theTrackAdvancesAndADieIsRolled() {
        execute(new TestTheLimitsEvent(1, TEST_THE_LIMITS_5));

        assertFollowup(all(single(new IncreaseJumpPreparationEvent()), rollDie(new TestTheLimitsResultEvent())));
    }

    // The -3 population space is the first blue one.
    @Test
    void notOnABlueSpace() {
        galacticaBoard.advanceJumpPreparation().advanceJumpPreparation().advanceJumpPreparation();

        assertInvalid(new TestTheLimitsEvent(1, TEST_THE_LIMITS_5));
    }
}
