package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.board.IncreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.TEST_THE_LIMITS;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.*;

public class TestTheLimitsEventProcessor extends SkillCardEventProcessor<TestTheLimitsEvent> {

    // The spaces before the blue ones (-3 and -1 population), as the track is printed. From these the token can't reach
    // a jump.
    private static final Set<JumpPreparation> NOT_BLUE = Set.of(START, POSITION_1, POSITION_2);

    public static boolean canPlay(GameView game) {
        return NOT_BLUE.contains(game.boards().galactica().jumpPreparation());
    }

    @Override
    public SkillCardType type() {
        return TEST_THE_LIMITS;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game);
    }

    @Override
    protected Followup resolve() {
        return all(single(new IncreaseJumpPreparationEvent()), rollDie(new TestTheLimitsResultEvent()));
    }
}
