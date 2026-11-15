package xyz.zlatanov.frakkintoasters.event.interrupt;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.STRATEGIC_PLANNING;

class MajorVictoryResultEventProcessorTest extends EventTestHarness<MajorVictoryResultEvent> {

    @Test
    void shouldGain1MoraleOn5OrHigher() {
        rolled(5);

        execute(new MajorVictoryResultEvent());

        assertFollowup(new GainResourceEvent(MORALE, 1));
    }

    @Test
    void shouldGainNothingBelow5() {
        rolled(4);

        execute(new MajorVictoryResultEvent());
    }

    @Test
    void shouldReadTheModifiedResult() {
        ops.startDieRoll();
        ops.playOnDieRoll(STRATEGIC_PLANNING, 2);
        nextRoll(3);
        ops.rollDie();

        execute(new MajorVictoryResultEvent());

        assertFollowup(new GainResourceEvent(MORALE, 1));
    }
}
