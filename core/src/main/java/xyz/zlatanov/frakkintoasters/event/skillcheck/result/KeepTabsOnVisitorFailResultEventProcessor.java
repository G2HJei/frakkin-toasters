package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;

public class KeepTabsOnVisitorFailResultEventProcessor extends EventProcessor<KeepTabsOnVisitorFailResultEvent> {

    @Override
    public Followup process() {
        return game.turn().dieRoll().result() <= 4
                ? single(lose(POPULATION, 2))
                : NONE;
    }
}
