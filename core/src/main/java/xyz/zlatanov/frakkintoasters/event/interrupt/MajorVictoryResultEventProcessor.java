package xyz.zlatanov.frakkintoasters.event.interrupt;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;

public class MajorVictoryResultEventProcessor extends EventProcessor<MajorVictoryResultEvent> {

    private static final int LOWEST_ROLL_FOR_MORALE = 5;

    @Override
    public Followup process() {
        return game.turn().dieRoll().result() >= LOWEST_ROLL_FOR_MORALE
                ? single(gain(MORALE, 1))
                : NONE;
    }
}
